package com.zekart.trackensurequbesdk.sdk.ext

import com.zekart.trackensurequbesdk.sdk.frame.BaseParse
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonTransformingSerializer

// how to @Serializable(with = PositionSerializer::class) val position: Position,

object InfoSerializer: JsonTransformingSerializer<BaseParse.BaseInfo>(tSerializer = BaseParse.BaseInfo.serializer()) {
    override fun transformDeserialize(element: JsonElement): JsonElement = JsonPrimitive(value = element.toString())
}

object PositionSerializer: JsonTransformingSerializer<BaseParse.BasePosition>(tSerializer = BaseParse.BasePosition.serializer()) {
    override fun transformDeserialize(element: JsonElement): JsonElement = JsonPrimitive(value = element.toString())
}

object TelemetrySerializer: JsonTransformingSerializer<BaseParse.BaseTelemetry>(tSerializer = BaseParse.BaseTelemetry.serializer()) {
    override fun transformDeserialize(element: JsonElement): JsonElement = JsonPrimitive(value = element.toString())
}

object SpnSerializer: JsonTransformingSerializer<BaseParse.BaseSpn>(tSerializer = BaseParse.BaseSpn.serializer()) {
    override fun transformDeserialize(element: JsonElement): JsonElement = JsonPrimitive(value = element.toString())
}