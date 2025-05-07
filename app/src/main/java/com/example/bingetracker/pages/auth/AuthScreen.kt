package com.example.bingetracker.pages.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bingetracker.R
import com.example.bingetracker.models.AuthModel
import com.example.bingetracker.models.AuthState

@Composable
fun AuthScreen(
    authViewModel: AuthModel,
    onAuthSuccess: () -> Unit
) {
    // Track if we're showing sign-up or sign-in
    var isSignUp by remember { mutableStateOf(false) }
    val authState by authViewModel.authState.collectAsState()

    // Navigate when authentication succeeds
    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onAuthSuccess()
        }
    }

    // Create a gradient background from dark blue to a slightly purple-blue
    val gradientColors = listOf(
        Color(0xFF0A0F3D),  // Darker blue at the top
        Color(0xFF141E61),  // Mid blue
        Color(0xFF0A1172)   // Slightly lighter blue with a hint of purple
    )

    // Main content
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = gradientColors
                )
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF000000).copy(alpha = 0.05f),  // Very subtle black
                            Color(0xFF000000).copy(alpha = 0.0f)    // Transparent
                        ),
                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                        end = androidx.compose.ui.geometry.Offset(1000f, 1000f)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            // App title
            Text(
                text = "Binge Tracker",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 38.sp,
                modifier = Modifier.padding(bottom = 40.dp)
            )

            Image(
                painter = painterResource(R.drawable.movie_icon),
                contentDescription = "",
                modifier = Modifier.padding(bottom = 40.dp)
                    .size(200.dp)
            )

//            Text(
//                text = "🎬",
//                fontSize = 72.sp,
//                modifier = Modifier.padding(bottom = 40.dp)
//            )

            // Login form
            if (isSignUp) {
                SignUpForm(authViewModel)
            } else {
                SignInForm(authViewModel)
            }

            // Error message
            if (authState is AuthState.Error) {
                Text(
                    text = (authState as AuthState.Error).message,
                    color = Color.Red,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            // Loading indicator
            if (authState is AuthState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(top = 16.dp),
                    color = Color.White
                )
            }

            // Sign up / Sign in switch
            Spacer(modifier = Modifier.weight(1f)) // Push to bottom

            TextButton(onClick = { isSignUp = !isSignUp }) {
                Text(
                    text = if (isSignUp) "Already have an account? Sign In" else "Sign Up",
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
fun SignInForm(authModel: AuthModel) {
    // Form state
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Email field with icon
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
                unfocusedTextColor = Color.Black,
                focusedTextColor = Color.Black
            ),
            shape = RoundedCornerShape(8.dp),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Email",
                    tint = Color.Gray
                )
            },
            placeholder = { Text("Email") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
        )

        // Password field with icon
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
                unfocusedTextColor = Color.Black,
                focusedTextColor = Color.Black
            ),
            shape = RoundedCornerShape(8.dp),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Password",
                    tint = Color.Gray
                )
            },
            placeholder = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            )
        )

        // Login button
        Button(
            onClick = { authModel.signInUser(email, password) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFAA00FF) // Purple color
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Login", fontSize = 16.sp)
        }
    }
}

@Composable
fun SignUpForm(authModel: AuthModel) {
    // Form state
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Name field with icon
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
                unfocusedTextColor = Color.Black,
                focusedTextColor = Color.Black
            ),
            shape = RoundedCornerShape(8.dp),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Name",
                    tint = Color.Gray
                )
            },
            placeholder = { Text("Name") },
            singleLine = true
        )

        // Email field with icon
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
                unfocusedTextColor = Color.Black,
                focusedTextColor = Color.Black
            ),
            shape = RoundedCornerShape(8.dp),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Email",
                    tint = Color.Gray
                )
            },
            placeholder = { Text("Email") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        // Password field with icon
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
                unfocusedTextColor = Color.Black,
                focusedTextColor = Color.Black
            ),
            shape = RoundedCornerShape(8.dp),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Password",
                    tint = Color.Gray
                )
            },
            placeholder = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        // Sign Up button
        Button(
            onClick = { authModel.signUpUser(name, email, password) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFAA00FF) // Purple color
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Sign Up", fontSize = 16.sp)
        }
    }
}