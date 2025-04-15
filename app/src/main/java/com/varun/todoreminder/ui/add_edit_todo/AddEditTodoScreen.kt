package com.varun.todoreminder.ui.add_edit_todo

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.varun.todoreminder.ui.theme.PrimaryColor
import com.varun.todoreminder.util.NotificationReceiver
import com.varun.todoreminder.util.UiEvent
import requestExactAlarmPermission
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTodoScreen(
    onPopBackStack: () -> Unit,
    viewModel: AddEditTodoViewModel = hiltViewModel()
) {
    val activity = LocalActivity.current
    SideEffect {
        val window = activity?.window
        val insetsController = window?.let { WindowInsetsControllerCompat(it, window.decorView) }
        insetsController?.isAppearanceLightStatusBars = true
    }

    val snackbarHostState = remember { SnackbarHostState() }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showRecurrenceDialog by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }

    val recurrenceOptions = listOf(
        "None",
        "Every 5 minutes",
        "Every 15 minutes",
        "Every 30 minutes",
        "Hourly",
        "Daily",
        "Weekly",
        "Monthly"
    )
    val context = LocalContext.current

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (!isGranted) {
                Toast.makeText(
                    context,
                    "Permission denied. Notifications won't work unless enabled.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    )

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                showPermissionDialog = true
            }
        }

        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.PopBackStack -> onPopBackStack()
                is UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        actionLabel = event.action
                    )
                }

                else -> Unit
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Add Task", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { onPopBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Button(
                    onClick = {
                        viewModel.onEvent(AddEditTodoEvent.OnSaveTodoClick)
                        viewModel.date?.let { date ->
                            viewModel.time?.let { time ->
                                scheduleNotification(
                                    date = date,
                                    time = time,
                                    context = context,
                                    todoId = viewModel.todo?.id ?: 0,
                                    title = viewModel.title
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Save", tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Task", color = Color.White)
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 15.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                OutlinedTextField(
                    value = viewModel.title,
                    onValueChange = { viewModel.onEvent(AddEditTodoEvent.OnTitleChange(it)) },
                    placeholder = { Text("Title") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
            }

            item {
                OutlinedTextField(
                    value = viewModel.description,
                    onValueChange = { viewModel.onEvent(AddEditTodoEvent.OnDescriptionChange(it)) },
                    placeholder = { Text("Description") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    singleLine = false,
                    maxLines = 5
                )
            }

            item {
                Text(
                    "Date & Time",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = "Date",
                            tint = PrimaryColor
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Date", fontSize = 14.sp, color = Color.Gray)
                            Text(viewModel.date ?: "Select Date", fontSize = 16.sp)
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showTimePicker = true },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Time",
                            tint = PrimaryColor
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Time", fontSize = 14.sp, color = Color.Gray)
                            Text(viewModel.time ?: "Select Time", fontSize = 16.sp)
                        }
                    }
                }
            }

            item {
                Text(
                    "Recurrence",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showRecurrenceDialog = true },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(viewModel.recurrence ?: "None", fontSize = 16.sp)
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = "Select Recurrence"
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val localDate = java.time.Instant.ofEpochMilli(millis)
                            .atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                        val formattedDate =
                            localDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
                        viewModel.onEvent(AddEditTodoEvent.OnDateChange(formattedDate))
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState()
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Select Time") },
            text = {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val localTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                    val formattedTime = localTime.format(DateTimeFormatter.ofPattern("hh:mm a"))
                    viewModel.onEvent(AddEditTodoEvent.OnTimeChange(formattedTime))
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Cancel") } }
        )
    }

    if (showRecurrenceDialog) {
        AlertDialog(
            onDismissRequest = { showRecurrenceDialog = false },
            title = { Text("Recurrence") },
            text = {
                Column {
                    recurrenceOptions.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.onEvent(AddEditTodoEvent.OnRecurrenceChange(option))
                                    showRecurrenceDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = viewModel.recurrence == option,
                                onClick = {
                                    viewModel.onEvent(AddEditTodoEvent.OnRecurrenceChange(option))
                                    showRecurrenceDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = option)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = {
                    showRecurrenceDialog = false
                }) { Text("Cancel") }
            }
        )
    }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Allow Notification Access") },
            text = { Text("To remind you about your tasks, we need permission to show notifications.") },
            confirmButton = {
                TextButton(onClick = {
                    showPermissionDialog = false
                    notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }) {
                    Text("Allow")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("Deny")
                }
            }
        )
    }
}

@SuppressLint("ScheduleExactAlarm")
fun scheduleNotification(
    date: String,
    time: String,
    context: Context,
    todoId: Int = 0,
    title: String = ""
) {
    try {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("title", if (title.isNotBlank()) title else "Reminder")
            putExtra("message", "You have a task scheduled at $time on $date.")
            putExtra("todoId", todoId)
        }

        val requestCode = if (todoId > 0) todoId else System.currentTimeMillis().toInt()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a")
        val localDateTime = LocalDateTime.parse("$date $time", formatter)

        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, localDateTime.year)
            set(Calendar.MONTH, localDateTime.monthValue - 1)
            set(Calendar.DAY_OF_MONTH, localDateTime.dayOfMonth)
            set(Calendar.HOUR_OF_DAY, localDateTime.hour)
            set(Calendar.MINUTE, localDateTime.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            Toast.makeText(
                context,
                "Please select a future time for the reminder",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            requestExactAlarmPermission(context)
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
            Toast.makeText(context, "Reminder set for $time on $date", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        Log.e("Notification", "Failed to schedule: ${e.message}")
        Toast.makeText(context, "Failed to set reminder", Toast.LENGTH_SHORT).show()
    }
}
