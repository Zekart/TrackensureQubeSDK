package com.zekart.trackensurequbesdk.sdk.types

enum class Quality {
    INVALID,
    GUESS,
    GOOD,
    STRONG,
    BEST,
    ESTIMATED;

    fun calc(): Accuracy {
        return when(this){
            INVALID, ESTIMATED -> Accuracy.NONE
            GUESS, GOOD -> Accuracy.NORMAL
            STRONG, BEST -> Accuracy.HIGH
        }
    }
}

enum class Accuracy{
    NONE, NORMAL, HIGH
/*
LAT:48.02 LNG:30.85 quality: 0 - нет сигналов для вычисления, не доверяем координатам
LAT:48.02 LNG:30.85 quality: 1 - нет сигналов спутников, координаты вычислены по инерциальным датчикам, можно доверять
LAT:48.02 LNG:30.85 quality: 2 - есть сигналы для вычисления координат со спутника(хотябы по одной из групировок), доверяем координатам(нормальная точность)
LAT:48.02 LNG:30.85 quality: 3 - есть сигналы для вычисления координат со спутника(группировок больше 3), доверяем координатам(повышенная точность)
LAT:48.02 LNG:30.85 quality: 4 - есть сигналы для вычисления координат со спутника(группировок больше 3) + есть данные коррекции от инерциальных датчиков), доверяем координатам(высокая точность)
LAT:48.02 LNG:30.85 quality: 5 - нет сигналов для вычисления, не доверяем координатам

Для данного модуля:
нормальная точность  - 2,5м
повышеная точность - 2,0 м
высокая точность - 1,5 (1,0) м
 */
}
/*
### Type of quality fixation

|Fix status|Description|
|-----------|-----------|
|`0`|No Fix / Invalid|
|`1`|Standard GPS (2D/3D)|
|`2`|Differential GPS|
|`6`|Estimated (DR) Fix|



|0|No Fix / Invalid|
|1|Dead reckoning only|
|2|2D-fix|
|3|3D-fix|
|4|GNSS + dead reckoning combined|
|5|Time only fix|
 */


