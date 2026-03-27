from datetime import datetime, timedelta


def weighted_moving_average(history, horizon=7):
    history = history or []
    quantities = [float(item.get('qty', 0)) for item in history if item.get('qty') is not None]
    if not quantities:
        quantities = [20.0, 22.0, 21.0, 23.0]
    window = quantities[-7:]
    weights = list(range(1, len(window) + 1))
    denominator = sum(weights)
    weighted_sum = sum(value * weight for value, weight in zip(window, weights))
    baseline = weighted_sum / denominator if denominator else sum(window) / len(window)
    start = datetime.now().date()
    result = []
    for offset in range(1, horizon + 1):
        prediction = round(baseline * (1 + ((offset - 1) % 3 - 1) * 0.03), 2)
        result.append({
            'targetDate': str(start + timedelta(days=offset)),
            'predictQty': prediction,
            'confidenceLower': round(prediction * 0.9, 2),
            'confidenceUpper': round(prediction * 1.1, 2)
        })
    return result
