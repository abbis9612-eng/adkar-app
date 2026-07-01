import urllib.request
import json
lat, lng = 35.5558, 45.4436
target = {'Fajr': '04:17', 'Sunrise': '05:37', 'Dhuhr': '12:07', 'Asr': '15:38', 'Maghrib': '18:31', 'Isha': '19:45'}
for method in range(16):
    url = f'https://api.aladhan.com/v1/timings/05-04-2026?latitude={lat}&longitude={lng}&method={method}&school=0'
    try:
        req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0"})
        with urllib.request.urlopen(req) as response:
            data = json.loads(response.read().decode())['data']['timings']
            print(f'Method {method}: Fajr={data["Fajr"]}, Sunrise={data["Sunrise"]}, Dhuhr={data["Dhuhr"]}, Asr={data["Asr"]}, Maghrib={data["Maghrib"]}, Isha={data["Isha"]}')
    except Exception as e:
        print(f'Method {method} failed: {e}')
