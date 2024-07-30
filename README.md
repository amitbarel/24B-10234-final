# AirQualitySensor - Air Quality Monitor App

![ic_launcher](https://github.com/user-attachments/assets/b9769ccc-11c9-431b-bfea-b546c0c157e6)

## Overview

The **AirQualitySensor** is an Android application designed to monitor and display the Air Quality Index (AQI) based on the user's location. 
It utilizes a foreground service to periodically sample the user's location, fetch air quality data, and update notifications with AQI information.

## Features
- **Foreground Service**: Continuously monitors the user's location and updates the air quality data.
- **Notifications**: Provides real-time notifications about air quality with visual indicators.
- **Location Updates**: Samples location at regular intervals to fetch the latest air quality data.
- **User Interface**: Displays latitude, longitude, and AQI on the main screen with color-coded pollution levels.

## Permissions
The app requires the following permissions:

- **ACCESS_FINE_LOCATION**: To get the precise location of the user.
- **ACCESS_COARSE_LOCATION**: To get approximate location information.
- **POST_NOTIFICATIONS**: To post notifications (for Android 13 and above).

## Foreground Service

The foreground service (`AirQualityService`) performs the following tasks:

- **Location Sampling**: Requests location updates at regular intervals.
- **Air Quality Fetching**: Fetches AQI data from the OpenWeatherMap API.
- **Notification Updates**: Updates the notification with the latest AQI information.

## User Interface

- **MainActivity**: Displays the current latitude, longitude, and AQI. The background color of the pollution window indicates the air quality status:
  - **Good**: Green
  - **Moderate**: Yellow
  - **Unhealthy**: Orange
  - **Very Unhealthy or Hazardous**: Red

## Demo
https://github.com/user-attachments/assets/eee17020-d021-4ca3-9192-d3c3c0f647f9

## Contributing
Amit Barel
