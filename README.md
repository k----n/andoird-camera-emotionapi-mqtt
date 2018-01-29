Android Camera + Emotion API + MQTT
============================

This is an Android camera app that allows photos taken from a camera, have the
image processed using Microsoft's Emotion API and result sent to an MQTT server.

This is a toy application to test out IoT functionality with Node-RED.

This app does not directly send the image via MQTT because the payload length
is limited when using IBM bluemix with Node-RED.

This code is based on https://github.com/xxv/mqtt_camera

Usage
-----

Install the app and configure MQTT for your server/broker. Once the app is
running and MQTT is configured, MQTT will attempt to connect whenever the app
is open and will disconnect when navigated away.

MQTT Messages
-------------

### Topic: `status`

This represents the MQTT connectivity status of the camera.

Payload:

* `connected` - the app is running and connected to the MQTT broker
* `disconnected` - the app has disconnected from the MQTT broker

### Topic: `image`

The payload of this message is the JPEG image taken by the camera.
