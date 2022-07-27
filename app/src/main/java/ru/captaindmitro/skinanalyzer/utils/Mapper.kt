package ru.captaindmitro.skinanalyzer.utils

import org.tensorflow.lite.support.label.Category
import org.tensorflow.lite.task.vision.classifier.Classifications

interface Mapper<T, V> {
    fun map(t: T): V

    class Lesion : Mapper<String, String> {
        override fun map(t: String): String {
            TODO("Not yet implemented")
        }

    }
}

fun Category.decode(): String = when (label) {
    "nv" -> "Невус"
    "mel" -> "Меланома"
    "bkl" -> "Злокачественный кератоз"
    "bcc" -> "Карцинома"
    "akiec" -> "Актинический кератоз"
    "vasc" -> "Васкулярное поражение"
    "df" -> "Дерматофиброма"
    else -> "Ошибка"
}
