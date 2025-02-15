package com.example.threenitas_project.ui.signIn

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.threenitas_project.R
import com.example.threenitas_project.network.ApiViewModel

@Composable
fun SignIn(
    apiViewModel: ApiViewModel,
    signInViewModel: SignInViewModel = viewModel(),
    navigateToBottomBar: () -> Unit
) {
    val scrollState = rememberScrollState()
    val uiState by signInViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState), //not needed but good to have
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PageTitle(stringResource(R.string.sign_in))

        Spacer(modifier = Modifier.weight(0.2f))

        CustomTextField(
            textState = uiState.userId,
            onValueChange = signInViewModel::changeNameText,
            title = stringResource(R.string.user_id),
            onClickInfo = signInViewModel::changeUserIdInfo,
            keyboardOption = KeyboardOptions(keyboardType = KeyboardType.Text),
            isPassword = false,
            isErrorInText = !uiState.isValidText,
            modifier = Modifier
        )

        Spacer(modifier = Modifier.padding(top = 35.dp))

        CustomTextField(
            textState = uiState.passwordText,
            onValueChange = signInViewModel::changePasswordText,
            title = stringResource(R.string.password),
            onClickInfo = signInViewModel::changePasswordInfo,
            keyboardOption = KeyboardOptions(keyboardType = KeyboardType.Password),
            isPassword = true,
            isErrorInText = !uiState.isValidPassword,
            modifier = Modifier,
        )

        Spacer(modifier = Modifier.weight(1f))
        OutlinedButton(
            onClick = {
                signInViewModel.changeLoadingSignIn(true)
                if (signInViewModel.checkSignIn()) {
                    // Only run login if sign-in checks pass
                    apiViewModel.login(uiState.userId, uiState.passwordText,navigateToBottomBar,signInViewModel::changeLoadingSignIn)
                }else{
                    signInViewModel.changeLoadingSignIn(false)
                }
            },
            modifier = Modifier
                .padding(bottom = 20.dp)
                .width(180.dp)
                .height(45.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            ),
            border = BorderStroke(
                2.dp,
                MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                stringResource(R.string.sign_in),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }

        //pop ups
        when {
            uiState.showWrongSignIn -> {
                PopUpWithButton(
                    title = stringResource(R.string.wrong_sign_in_title),
                    smallText = stringResource(R.string.wrong_sign_in_body),
                    hide = signInViewModel::hideShowWrongSignIn,
                    buttonText = stringResource(R.string.back)
                )
            }

            uiState.userIdInfo -> {
                PopUpSimple(
                    textValue = stringResource(R.string.user_req),
                    hide = { signInViewModel.changeUserIdInfo(false) }
                )
            }

            uiState.passwordInfo -> {
                PopUpSimple(
                    textValue = stringResource(R.string.pass_req),
                    hide = { signInViewModel.changePasswordInfo(false) }
                )
            }

            uiState.loadingSignIn -> {
                Dialog(onDismissRequest = {}) {
                    Column {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@Composable
fun PageTitle(
    name: String,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(MaterialTheme.colorScheme.onBackground),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.titleLarge,
            color = if (isDarkTheme) Color.White else Color.Black,
            fontWeight = FontWeight.Bold // this can be inside style
        )
    }
}

@Composable
fun CustomTextField(
    textState: String,
    onValueChange: (String) -> Unit,
    title: String,
    onClickInfo: (Boolean) -> Unit,
    keyboardOption: KeyboardOptions,
    isPassword: Boolean,
    modifier: Modifier,
    isErrorInText: Boolean,
) {
    var passwordVisible by remember { mutableStateOf(false) }

    TextField(
        value = textState,
        maxLines = 1,
        modifier = modifier,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            errorContainerColor = Color.Transparent,
        ),
        isError = isErrorInText,
        onValueChange = onValueChange,
        label = {
            Row {
                Text(
                    title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.padding(start = 5.dp))
                IconButton(
                    onClick = { onClickInfo(true) },
                    modifier = Modifier.size(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = stringResource(R.string.info_icon),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        },
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = keyboardOption,
        trailingIcon = {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (isPassword) {
                    Text(
                        text = if (passwordVisible) stringResource(R.string.hide) else stringResource(
                            R.string.show
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { passwordVisible = !passwordVisible }
                    )
                }

                if (isErrorInText) {
                    Text(
                        text = "!",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    )
}

@Composable
fun PopUpWithButton(
    title: String,
    smallText: String,
    hide: () -> Unit,
    buttonText: String
) {
    Dialog(onDismissRequest = hide) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    Modifier.padding(start = 10.dp, end = 10.dp, top = 30.dp, bottom = 30.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = smallText,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.primary
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { hide() },
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.padding(7.dp))
                    Text(
                        text = buttonText,
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.padding(7.dp))
                }
            }
        }
    }
}

@Composable
fun PopUpSimple(
    textValue: String,
    hide: () -> Unit,
) {
    Dialog(onDismissRequest = hide) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(start = 10.dp, end = 10.dp, top = 30.dp, bottom = 30.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                Text(
                    text = textValue,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}