package util

import org.apache.flink.api.common.functions.RuntimeContext
import org.apache.flink.api.common.state.ListState
import org.apache.flink.api.common.state.ListStateDescriptor
import org.apache.flink.api.common.state.ValueState
import org.apache.flink.api.common.state.ValueStateDescriptor


inline fun <reified T: Any> RuntimeContext.getValueState(name: String): ValueState<T> = getState(ValueStateDescriptor(name, T::class.java))
inline fun <reified T: Any>  RuntimeContext.getListState(name: String): ListState<T> = getListState(ListStateDescriptor(name, T::class.java))