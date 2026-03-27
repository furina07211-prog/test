import datetime as dt
import re
from collections import defaultdict


def load_history(payload):
    fruit_id = payload.get('fruitId')
    if fruit_id is None:
        return []

    history = load_history_from_mysql(payload)
    if history:
        return history

    sql_path = payload.get('testDataSqlPath')
    if sql_path:
        return load_history_from_sql(sql_path, int(fruit_id))

    return []


def load_history_from_mysql(payload):
    try:
        import pymysql
    except Exception:
        return []

    db = payload.get('db') or {}
    required = ['host', 'port', 'user', 'password', 'database']
    if any(k not in db or db.get(k) in (None, '') for k in required):
        return []

    conn = None
    try:
        conn = pymysql.connect(
            host=db['host'],
            port=int(db['port']),
            user=db['user'],
            password=db['password'],
            database=db['database'],
            charset=db.get('charset', 'utf8mb4'),
            cursorclass=pymysql.cursors.DictCursor,
        )
        with conn.cursor() as cursor:
            cursor.execute(
                """
                SELECT DATE(so.order_time) AS sale_date, SUM(soi.quantity) AS qty
                FROM sales_order so
                JOIN sales_order_item soi ON so.id = soi.sales_order_id
                WHERE soi.fruit_id = %s
                GROUP BY DATE(so.order_time)
                ORDER BY DATE(so.order_time)
                """,
                (int(payload['fruitId']),),
            )
            rows = cursor.fetchall() or []
            return [
                {'date': str(row['sale_date']), 'qty': float(row['qty'] or 0)}
                for row in rows
            ]
    except Exception:
        return []
    finally:
        if conn:
            conn.close()


def load_history_from_sql(sql_path, fruit_id):
    try:
        with open(sql_path, 'r', encoding='utf-8', errors='ignore') as file:
            content = file.read()
    except Exception:
        return []

    sales_orders = parse_sales_orders(content)
    sales_items = parse_sales_items(content)
    daily = defaultdict(float)

    for item in sales_items:
        if item['fruit_id'] != fruit_id:
            continue
        sale_date = sales_orders.get(item['sales_order_id'])
        if not sale_date:
            continue
        daily[sale_date] += item['quantity']

    return [
        {'date': sale_date, 'qty': round(qty, 2)}
        for sale_date, qty in sorted(daily.items(), key=lambda x: x[0])
    ]


def parse_sales_orders(content):
    mapping = {}
    values_text = extract_values_block(content, 'sales_order')
    if not values_text:
        return mapping

    for row in split_rows(values_text):
        cols = split_columns(row)
        if len(cols) < 6:
            continue
        try:
            order_id = int(clean_scalar(cols[0]))
            order_time = clean_scalar(cols[5])
            date_part = str(order_time).split(' ')[0]
            dt.datetime.strptime(date_part, '%Y-%m-%d')
            mapping[order_id] = date_part
        except Exception:
            continue
    return mapping


def parse_sales_items(content):
    result = []
    values_text = extract_values_block(content, 'sales_order_item')
    if not values_text:
        return result

    for row in split_rows(values_text):
        cols = split_columns(row)
        if len(cols) < 5:
            continue
        try:
            result.append(
                {
                    'sales_order_id': int(clean_scalar(cols[1])),
                    'fruit_id': int(clean_scalar(cols[2])),
                    'quantity': float(clean_scalar(cols[4])),
                }
            )
        except Exception:
            continue
    return result


def extract_values_block(content, table_name):
    pattern = re.compile(
        r"INSERT INTO\s+" + re.escape(table_name) + r"\s*\([^)]*\)\s*VALUES\s*(.*?);",
        re.IGNORECASE | re.DOTALL,
    )
    match = pattern.search(content)
    if not match:
        return ''
    return match.group(1).strip()


def split_rows(values_text):
    rows = []
    depth = 0
    in_quote = False
    start = None
    index = 0
    while index < len(values_text):
        ch = values_text[index]
        if ch == "'" and (index == 0 or values_text[index - 1] != '\\'):
            in_quote = not in_quote
        if not in_quote:
            if ch == '(':
                if depth == 0:
                    start = index + 1
                depth += 1
            elif ch == ')':
                depth -= 1
                if depth == 0 and start is not None:
                    rows.append(values_text[start:index])
                    start = None
        index += 1
    return rows


def split_columns(row_text):
    cols = []
    in_quote = False
    current = []
    index = 0
    while index < len(row_text):
        ch = row_text[index]
        if ch == "'" and (index == 0 or row_text[index - 1] != '\\'):
            in_quote = not in_quote
        if ch == ',' and not in_quote:
            cols.append(''.join(current).strip())
            current = []
        else:
            current.append(ch)
        index += 1
    if current:
        cols.append(''.join(current).strip())
    return cols


def clean_scalar(value):
    item = value.strip()
    if item.upper() == 'NULL':
        return ''
    if len(item) >= 2 and item[0] == "'" and item[-1] == "'":
        return item[1:-1]
    return item