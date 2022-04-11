package io.almer.blog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.almer.api.User
import io.almer.blog.ui.theme.InternshipTheme
import org.lighthousegames.logging.logging

private val Log = logging("MainActivity")

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            InternshipTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    Users()
                }
            }
        }
    }
}

@Composable
fun Users() {
    // we use the `val (a, setA) = remember` because it is more obvious for a beginner to figure out what it does
    val (errorMessage, setErrorMessage) = remember {
        mutableStateOf<String?>(null)
    }

    val (users, setUsers) = remember {
        mutableStateOf<List<User>?>(null)
    }


    // we launch a coroutine (asynchronous task) to get the users from the backend
    LaunchedEffect(true) {
        val reply = apiClient.userApi.getUser()

        val err = reply.exceptionOrNull()

        if (err != null) {
            err.printStackTrace()
            // actually in Kotlin "if" operator is an expression, it will "return" the result of each branch
            // so there is a nicer way to write this, but it's not important for this test
            if (err.message != null) {
                setErrorMessage(err.message)
            } else {
                setErrorMessage("Unknown error")
            }
        } else {
            setUsers(reply.getOrThrow())
        }

        /* the whole function can be actually compacted as
            users = reply.getOrElse {
                errorMessage = it.message ?: "Unknown error"
                return@LaunchedEffect
            }
            but there is already too much information
         */

    }

    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        if (errorMessage != null) {
            // we have an error
            Column {
                Text("Error", color = Color.Red)
                Text(errorMessage, color = Color.Red)
            }
        } else if (users == null) {
            // the data has not loaded yet, show a loading screen
            Box(contentAlignment = Alignment.Center) {
                Text("Loading")
            }
        } else {
            // data has loaded
            LazyColumn {
                users.forEach {
                    item {
                        Card {
                            Column {
                                Row(Modifier.fillMaxWidth()) {
                                    Text("Name", modifier = Modifier.weight(1f))
                                    Text("${it.firstName} ${it.lastName}")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}
