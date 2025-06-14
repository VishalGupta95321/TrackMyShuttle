package util

import org.apache.flink.api.common.functions.RuntimeContext
import org.apache.flink.api.common.state.ListState
import org.apache.flink.api.common.state.ListStateDescriptor
import org.apache.flink.api.common.state.ValueState
import org.apache.flink.api.common.state.ValueStateDescriptor
import org.apache.flink.api.common.typeinfo.TypeHint
import org.apache.flink.api.java.typeutils.TypeExtractor


inline fun <reified T: Any> RuntimeContext.getValueState(name: String): ValueState<T> {
    val typeInfo = object : TypeHint<T>() {}.typeInfo
    return getState(ValueStateDescriptor(name, typeInfo))
}

inline fun <reified T: Any>  RuntimeContext.getListState(name: String): ListState<T> {
    val typeInfo = object : TypeHint<T>() {}.typeInfo
    return getListState(ListStateDescriptor(name, typeInfo))
}