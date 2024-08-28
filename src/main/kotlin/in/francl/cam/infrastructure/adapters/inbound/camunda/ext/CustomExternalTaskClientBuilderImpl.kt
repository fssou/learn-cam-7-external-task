package `in`.francl.cam.infrastructure.adapters.inbound.camunda.ext

import org.camunda.bpm.client.impl.ExternalTaskClientBuilderImpl

class CustomExternalTaskClientBuilderImpl : ExternalTaskClientBuilderImpl() {
    override fun initTopicSubscriptionManager() {
        topicSubscriptionManager = CustomTopicSubscriptionManager(engineClient, typedValues, lockDuration)
        topicSubscriptionManager.setBackoffStrategy(getBackoffStrategy())
        if (isBackoffStrategyDisabled) {
            topicSubscriptionManager.disableBackoffStrategy()
        }
        if (isAutoFetchingEnabled()) {
            topicSubscriptionManager.start()
        }
    }
}