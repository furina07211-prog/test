import json
import sys

from data_loader import load_history
from forecast_prophet import forecast_with_prophet
from forecast_simple import weighted_moving_average
from generate_purchase_suggestion import generate_purchase_suggestion


def handle_forecast(payload):
    model = payload.get('model', 'prophet')
    days = int(payload.get('days', 7))
    history = payload.get('history') or load_history(payload)

    if model == 'prophet':
        predictions = forecast_with_prophet(history, days)
    else:
        predictions = weighted_moving_average(history, days)

    return {
        'fruitId': payload.get('fruitId'),
        'model': model,
        'days': days,
        'history': history,
        'predictions': predictions,
        'fallback': bool(not history),
    }


def handle_optimize(payload):
    return generate_purchase_suggestion(
        payload.get('predictedDailyQty'),
        payload.get('leadTimeDays'),
        payload.get('safeStockQty'),
        payload.get('currentStockQty'),
        payload.get('inTransitQty'),
    )


def main():
    mode = sys.argv[1] if len(sys.argv) > 1 else 'forecast'
    payload = json.loads(sys.stdin.read() or '{}')

    if mode == 'optimize':
        print(json.dumps(handle_optimize(payload), ensure_ascii=False))
        return

    print(json.dumps(handle_forecast(payload), ensure_ascii=False))


if __name__ == '__main__':
    main()