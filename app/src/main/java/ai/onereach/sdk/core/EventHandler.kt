package ai.onereach.sdk.core

abstract class EventHandler {

    abstract fun onHandleEvent(params: Map<String, Any>?)
}