package com.zekart.trackensurequbesdk.sdk.types

class Command {
    companion object {
        const val UNKNOWN = -1
        const val INFO = 0
        const val INFO_ACCEPT = 1
        const val FRAME = 2
        const val FRAME_ACCEPT = 3
        const val RESET = 4
    }
}

/*
INFO         |`0`|Invitation to work|
INFO_ACCEPT  |`1`|Acceptance of an Invitation to Work|
FRAME        |`2`|Contains a complete description of the vehicle’s condition|
FRAME_ACCEPT |`3`|The _Mobile_ successfully received the data packet|
 */