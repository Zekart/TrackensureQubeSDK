package com.zekart.trackensurequbesdk.qubesdk

import android.content.IntentFilter

class CustomWrapperQubeSDK {
    val intentFiltersSDK: IntentFilter? = WherequbeModel.getWherequbeEventsIntentFilter()
    val qubeModelIntents = WherequbeModel()
}