# OpenRouter API key setup (DO NOT commit your real key)

This project uses a Cloud Function `openRouterProxy` that reads the OpenRouter API key from an environment variable named `OPENROUTER_API_KEY`.

Recommended ways to set it (choose one):

1) Firebase / gcloud deploy (recommended)

- Using `gcloud` when deploying functions:

```bash
# Example (replace <KEY> with your actual key locally)
# Deploy the function and set env var
gcloud functions deploy openRouterProxy \
  --region=us-central1 \
  --runtime=nodejs20 \
  --trigger-http \
  --entry-point=openRouterProxy \
  --set-env-vars OPENROUTER_API_KEY="<YOUR_OPENROUTER_KEY>"
```

- Or set it in the Cloud Console > Cloud Functions > Configuration > Environment variables.

2) Firebase emulator (local development)

- Use a local environment before starting the emulator:

```bash
# Windows (PowerShell)
$env:OPENROUTER_API_KEY = "<YOUR_OPENROUTER_KEY>"
firebase emulators:start --only functions
```

3) Using Secret Manager (recommended for production)

- Store the key in Secret Manager and grant the Functions service account access. Then read the secret in your function at runtime.

Notes:
- Do NOT add the key to the repository (no `.env` or `functions/keys.json` committed).
- The function will return HTTP 500 if `OPENROUTER_API_KEY` is not configured.

If you want, I can set up a small helper script to deploy the function with the variable, but I will never commit the secret into the repo.