package util

sealed class EitherOfThree<A, B, C> {
    data class Left<A>(
        val value : A,
    ): EitherOfThree<A, Nothing, Nothing>()

    data class Middle<B>(
        val value : B,
    ): EitherOfThree<Nothing, B, Nothing>()

    data class Right<C>(
        val value: C,
    ): EitherOfThree<Nothing, Nothing, C>()

}