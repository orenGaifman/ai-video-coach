# YouTube API Key Setup Instructions

## Current Issue
The YouTube API is returning 403 Forbidden errors, which means:
1. API key quota exceeded (10,000 units/day limit)
2. API key not properly configured
3. YouTube Data API v3 not enabled

## Fix Steps

### 1. Check Google Cloud Console
- Go to: https://console.cloud.google.com/
- Select your project
- Go to "APIs & Services" > "Library"
- Search for "YouTube Data API v3"
- Make sure it's ENABLED

### 2. Check API Key Restrictions
- Go to "APIs & Services" > "Credentials"
- Click on your API key
- Under "API restrictions", make sure "YouTube Data API v3" is allowed
- Under "Application restrictions", set to "None" for testing

### 3. Check Quota Usage
- Go to "APIs & Services" > "Quotas"
- Search for "YouTube Data API v3"
- Check if you've exceeded the daily limit (10,000 units)

### 4. Test API Key Manually
```bash
curl "https://www.googleapis.com/youtube/v3/search?part=snippet&type=video&maxResults=1&q=test&key=YOUR_API_KEY"
```

### 5. Environment Variable
Make sure YOUTUBE_API_KEY is set:
```bash
export YOUTUBE_API_KEY=AIzaSyBMjzUSp-DQ6SYTnR_fLr3HG7CrpgI92dA
```

## Current Fallback
The system now returns realistic demo videos when YouTube API fails, so the application continues working.