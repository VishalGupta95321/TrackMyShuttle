package util

sealed class EitherOfThree<A, B, C> {
    data class BusStop<A>(
        val value : A,
    ): EitherOfThree<A, Nothing, Nothing>()

    data class Bus<B>(
        val value : B,
    ): EitherOfThree<Nothing, B, Nothing>()

    data class Route<C>(
        val value: C,
    ): EitherOfThree<Nothing, Nothing, C>()

}