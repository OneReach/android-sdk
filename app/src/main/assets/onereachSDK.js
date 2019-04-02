var eventHandlers = {};

<!--     Register native event handler-->
<!--     Parameter handler - function to be triggered by event-->
<!--     Parameter eventName - name of event-->
function registerEventHandler(eventName, handler) {
    eventHandlers[eventName] = handler;
};


<!--     Unregister native event handler-->
<!--     Parameter eventName - name of event-->
function unregisterEventHandler(eventName) {
    eventHandlers[eventName] = nil;
};


<!--    Invoked when a event is received from a native-->
<!--    Parameter eventName - name of received event-->
<!--    Parameter parameters - event parameters-->
function handleEvent(eventName, parameters) {
    eventHandlers[eventName](parameters);
};

<!--    Send event to native-->
<!--    Parameter eventName - name of event-->
<!--    Parameter parameters - native handler parameters.-->
function sendEvent(eventName, parameters) {
    // The next line works correctly on iOS but it stuck/stop/break this method for Android
    // window.webkit.messageHandlers.OneReach.postMessage({"name":eventName, "parameters":parameters});

    // Android: actually 'parameters' is 'undefined' and passed to Android like 'undefined' string,
    // parsed to JSON and throws JsonEncodingException by Moshi library
    AndroidOneReachInterface.callEvent(eventName, parameters);

    // Android: 'parameters' passed as null and don't parsed as JSON
    //AndroidOneReachInterface.callEvent(eventName, null);

    // Android: pass hardcoded Map to Android in JSON string format,
    // parsed to JSON and converted to Map<String, Any> by Moshi library
    //AndroidOneReachInterface.callEvent(eventName, JSON.stringify({ Name : 'Test Value Name', Age: 100}));
};
