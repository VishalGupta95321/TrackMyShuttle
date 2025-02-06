package data.util

import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

class ClassIntrospector< T: Any>(
    val clazz: KClass<T>,
)  {
    fun inspectAttr(attrName: String):Boolean {
        return clazz.memberProperties.find { it.name == attrName } != null
    }

    fun inspectAttrAndReturnType(attrName: String):KClass<*>?{
        val property = clazz.memberProperties.find { it.name == attrName  }
        return property?.returnType?.classifier as? KClass<*>
    }

    inline fun <reified T: Annotation> getAttrNameByAnnotation(): String? {
        return clazz.memberProperties.find {
            it.findAnnotation<T>() != null
        }?.name
    }
}
