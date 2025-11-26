
<!-- more -->
index.html
```
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>IP 定位示例</title>
    <style>
        body {
            font-family: Arial, sans-serif;
        }
        #location {
            margin: 20px;
            padding: 20px;
            border: 1px solid #ddd;
            border-radius: 4px;
            background-color: #f9f9f9;
        }
        #map {
            width: 100%;
            height: 400px;
            margin-top: 20px;
        }
    </style>
</head>
<body>

    <h1>IP 定位</h1>
    <div id="location">
        <p>IP 地址: <span id="ip"></span></p>
        <p>城市: <span id="city"></span></p>
        <p>地区: <span id="region"></span></p>
        <p>国家: <span id="country"></span></p>
        <p>经度: <span id="longitude"></span></p>
        <p>纬度: <span id="latitude"></span></p>
    </div>
    <div id="map"></div>

    <script>
        function loadLocationData() {
            fetch('https://ipapi.co/json/')
                .then(response => response.json())
                .then(data => {
                    document.getElementById('ip').textContent = data.ip;
                    document.getElementById('city').textContent = data.city;
                    document.getElementById('region').textContent = data.region;
                    document.getElementById('country').textContent = data.country_name;
                    document.getElementById('longitude').textContent = data.longitude;
                    document.getElementById('latitude').textContent = data.latitude;

                    loadMap(data.latitude, data.longitude);
                })
                .catch(error => console.error('Error fetching IP data:', error));
        }

        function loadMap(latitude, longitude) {
            const map = new google.maps.Map(document.getElementById('map'), {
                center: { lat: latitude, lng: longitude },
                zoom: 8
            });
            new google.maps.Marker({
                position: { lat: latitude, lng: longitude },
                map: map
            });
        }

        document.addEventListener('DOMContentLoaded', loadLocationData);
    </script>
    <script async defer
        src="https://maps.googleapis.com/maps/api/js?key=YOUR_GOOGLE_MAPS_API_KEY">
    </script>

</body>
</html>

```


![20241129225646](https://liu-fu-gui.github.io/myimg/halo/20241129225646.png)