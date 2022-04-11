package components

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

@Composable
fun AlmerLogo() {
    Img(src = "symbol.svg", alt = "Logo") {
        style {
            width(300.px)
            height(300.px)
        }
    }
}