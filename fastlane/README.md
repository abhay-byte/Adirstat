# Fastlane Setup Guide

This guide explains how to set up and use Fastlane for CI/CD automation of the Adirstat Android app.

---

## Prerequisites

### 1. Install Fastlane

```bash
# Using RubyGems
sudo gem install fastlane

# Or using Homebrew on macOS
brew install fastlane
```

### 2. Verify Installation

```bash
fastlane --version
```

### 3. Install Bundler (optional but recommended)

```bash
# Create Gemfile
echo 'source "https://rubygems.org"' > Gemfile
echo 'gem "fastlane"' >> Gemfile
bundle install
```

---

## Environment Variables

Fastlane requires several environment variables for different operations:

### Required Variables

| Variable | Description | Required For |
|----------|-------------|--------------|
| `KEYSTORE_FILE` | Path to keystore file | `build_release` |
| `KEYSTORE_PASSWORD` | Keystore password | `build_release` |
| `KEY_ALIAS` | Key alias name | `build_release` |
| `KEY_PASSWORD` | Key password | `build_release` |
| `GOOGLE_PLAY_KEY_JSON` | Path to Play Store service account JSON | `deploy_*` lanes |
| `SLACK_WEBHOOK_URL` | Slack webhook URL for notifications | `send_slack_notification` |

### Setting Environment Variables

```bash
# Add to ~/.bashrc or ~/.zshrc
export KEYSTORE_FILE="/path/to/keystore.jks"
export KEYSTORE_PASSWORD="your_keystore_password"
export KEY_ALIAS="your_key_alias"
export KEY_PASSWORD="your_key_password"
export GOOGLE_PLAY_KEY_JSON="/path/to/google-play-key.json"
export SLACK_WEBHOOK_URL="https://hooks.slack.com/services/xxx"
```

---

## Available Lanes

### Test and Analysis

| Lane | Command | Description |
|------|---------|-------------|
| test | `fastlane test` | Run unit tests via Gradle |
| lint | `fastlane lint` | Run Android Lint analysis |

### Build

| Lane | Command | Description |
|------|---------|-------------|
| build_debug | `fastlane build_debug` | Assemble debug APK |
| build_release | `fastlane build_release` | Assemble signed release AAB |

### Deployment

| Lane | Command | Description |
|------|---------|-------------|
| deploy_internal | `fastlane deploy_internal` | Build and upload to internal track |
| deploy_alpha | `fastlane deploy_alpha` | Upload to alpha track |
| deploy_beta | `fastlane deploy_beta` | Upload to beta track |
| deploy_production | `fastlane deploy_production` | Upload to production (with confirmation) |
| deploy_fdroid | `fastlane deploy_fdroid` | Run tests + create git tag for F-Droid |

### Utilities

| Lane | Command | Description |
|------|---------|-------------|
| increment_version | `fastlane increment_version` | Bump versionCode and create git tag |
| send_slack_notification | `fastlane send_slack_notification` | Post to Slack |

---

## Usage Examples

### Running Tests

```bash
fastlane test
```

### Building Debug APK

```bash
fastlane build_debug
```

### Deploying to Internal Track

```bash
# Set up environment variables first
export KEYSTORE_FILE="..."
export GOOGLE_PLAY_KEY_JSON="..."

fastlane deploy_internal
```

### Deploying to F-Droid

```bash
# F-Droid uses git tags for building
# Make sure your code is committed
git add -A
git commit -m "Release v1.0.0"

fastlane deploy_fdroid
# This will:
# 1. Run tests
# 2. Run lint
# 3. Create git tag v1.0.0
# 4. Push the tag
# F-Droid will auto-build from the tag
```

### Production Deployment

```bash
export KEYSTORE_FILE="..."
export KEYSTORE_PASSWORD="..."
export KEY_ALIAS="..."
export KEY_PASSWORD="..."
export GOOGLE_PLAY_KEY_JSON="..."

fastlane deploy_production
# Will ask for confirmation before deploying
```

---

## Play Store Setup

### 1. Create Service Account

1. Go to Google Play Console
2. Navigate to: Release > Setup > API access
3. Click "Create new service account"
4. Follow the prompts to create a service account in Google Cloud
5. Download the JSON key file
6. Grant the service account "Release Manager" role

### 2. Add JSON Key to Fastlane

```bash
# Copy JSON key to project
cp /path/to/key.json fastlane/google-play-key.json

# Add to .gitignore
echo "fastlane/google-play-key.json" >> .gitignore
```

### 3. Permissions Declaration

For Adirstat, you need to complete the Permissions Declaration Form for:
- `MANAGE_EXTERNAL_STORAGE` (All Files Access)
- `QUERY_ALL_PACKAGES` (Query All Packages)

---

## Signing Config

### Creating a Keystore

```bash
keytool -genkeypair -v -storetype PKCS12 -keystore adirstat.keystore -alias adirstat -keyalg RSA -keysize 2048 -validity 10000
```

### Environment Setup

```bash
export KEYSTORE_FILE="$(pwd)/adirstat.keystore"
export KEYSTORE_PASSWORD="your_keystore_password"
export KEY_ALIAS="adirstat"
export KEY_PASSWORD="your_key_password"
```

---

## F-Droid Release Process

F-Droid builds from git tags. To release:

1. **Ensure all changes are committed:**
   ```bash
   git add -A
   git commit -m "Release v1.0.0"
   ```

2. **Run the F-Droid lane:**
   ```bash
   fastlane deploy_fdroid
   ```

3. **What happens:**
   - Tests run
   - Lint runs
   - Git commit created
   - Git tag `v1.0.0` created
   - Tag pushed to remote

4. **F-Droid automatically:**
   - Detects the new tag
   - Builds the APK
   - Publishes to F-Droid repository

---

## Adding Changelogs

Create changelog files in `fastlane/metadata/android/en-US/changelogs/`:

```bash
# Version 2 changelog
touch fastlane/metadata/android/en-US/changelogs/2.txt
```

Format: Plain text, max 500 characters per version.

---

## Troubleshooting

### "Missing signing config" error

Make sure all signing environment variables are set:
```bash
echo $KEYSTORE_FILE
echo $KEYSTORE_PASSWORD
echo $KEY_ALIAS
echo $KEY_PASSWORD
```

### "json_key_file not found" error

Ensure the JSON key file exists:
```bash
ls -la fastlane/google-play-key.json
```

### Play Store upload fails

1. Check service account has correct permissions
2. Verify the package name matches in Play Console
3. Ensure version code is higher than previous release

### Slow builds

Increase Gradle memory in the lane:
```ruby
gradle(
  task: "assembleDebug",
  gradle_properties: {
    "org.gradle.jvmargs" => "-Xmx4096m -XX:MaxMetaspaceSize=1024m"
  }
)
```

---

## CI/CD Integration

### GitHub Actions Example

```yaml
name: Android CI

on:
  push:
    branches: [main]
  pull_request:
    branches: [main]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK
        uses: actions/setup-java@v3
        with:
          java-version: '17'
      - name: Run tests
        run: fastlane test

  deploy:
    needs: test
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK
        uses: actions/setup-java@v3
        with:
          java-version: '17'
      - name: Deploy to Internal
        env:
          KEYSTORE_FILE: ${{ secrets.KEYSTORE_FILE }}
          KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
          KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
          KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
          GOOGLE_PLAY_KEY_JSON: ${{ secrets.GOOGLE_PLAY_KEY_JSON }}
        run: fastlane deploy_internal
```

---

## Files Reference

| File | Purpose |
|------|---------|
| `fastlane/Appfile` | Package name and JSON key configuration |
| `fastlane/Fastfile` | All CI/CD lanes |
| `fastlane/Pluginfile` | Fastlane plugins |
| `fastlane/metadata/android/` | Store listing metadata |
| `fastlane/README.md` | This file |

---

## Security Notes

- Never commit keystore files or JSON keys to git
- Use environment variables or secrets management
- Add sensitive files to `.gitignore`
- Rotate keys periodically
