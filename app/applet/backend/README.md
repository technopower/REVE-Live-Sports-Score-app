# FastAPI Backend Deployment

This is the FastAPI backend for the Android app.

## Deployment to Render

1.  **Repository**: Connect your GitHub repository containing the `backend/` directory to Render.
2.  **Environment**: 
    - Set `PYTHON_VERSION` to `3.11` (or latest).
    - Add required environment variables:
        - `SPORTMONKS_API_TOKEN`: Your Sportmonks API Token.
        - `PORT`: 8000 (Render will handle this).
3.  **Build Command**: `pip install -r requirements.txt`
4.  **Start Command**: `uvicorn backend.main:app --host 0.0.0.0 --port $PORT`
5.  **Health Check**: Ensure your Render service has a path `/health` configured for health checks.
