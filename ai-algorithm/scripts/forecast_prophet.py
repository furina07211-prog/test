from forecast_simple import weighted_moving_average


def forecast_with_prophet(history, horizon=7):
    try:
        import pandas as pd
        from prophet import Prophet
    except Exception:
        return weighted_moving_average(history, horizon)

    rows = history or []
    if not rows:
        return weighted_moving_average(history, horizon)

    df = pd.DataFrame([{'ds': row['date'], 'y': float(row['qty'])} for row in rows])
    if len(df) < 3:
        return weighted_moving_average(history, horizon)

    model = Prophet(daily_seasonality=True, weekly_seasonality=True)
    model.fit(df)
    future = model.make_future_dataframe(periods=horizon)
    forecast = model.predict(future).tail(horizon)

    results = []
    for _, row in forecast.iterrows():
        yhat = float(row.yhat)
        lower = float(row.yhat_lower)
        upper = float(row.yhat_upper)

        # Fruit sales cannot be negative; clamp model output for stable business use.
        yhat = max(yhat, 0.0)
        lower = max(lower, 0.0)
        upper = max(upper, 0.0)
        if upper < lower:
            upper = lower

        results.append(
            {
                'targetDate': str(row.ds.date()),
                'predictQty': round(yhat, 2),
                'confidenceLower': round(lower, 2),
                'confidenceUpper': round(upper, 2)
            }
        )

    return results