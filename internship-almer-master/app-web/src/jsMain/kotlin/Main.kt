import androidx.compose.runtime.*
import components.AlmerLogo
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.renderComposable
import org.lighthousegames.logging.logging

private val Log = logging("Main")

fun main() {
    renderComposable(rootElementId = "root") {
        Hello()
        Counter()
    }
}


@Composable
fun Hello() {
    var shouldSayHello by remember {
        mutableStateOf(false)
    }

    Div {
        Button(attrs = {
            onClick { shouldSayHello = !shouldSayHello }
        }) {
            AlmerLogo()
        }

        if (shouldSayHello) {
            Text("Hello there!")
        }
    }

}

@Composable
fun Counter() {
    var count: Int by remember {
        mutableStateOf(0)
    }


    Div({ style { padding(25.px) } }) {

        Button(attrs = {
            onClick { count -= 1 }
        }) {
            Text("-")
        }

        Span({ style { padding(15.px) } }) {
            Text("$count")
        }

        Button(attrs = {
            onClick { count += 1 }
        }) {
            Text("+")
        }

    }
}