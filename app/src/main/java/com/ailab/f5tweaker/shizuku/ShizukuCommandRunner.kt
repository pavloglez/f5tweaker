package com.ailab.f5tweaker.shizuku

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuRemoteProcess
import java.io.BufferedReader
import java.io.InputStreamReader

interface CommandRunner {
    suspend fun execute(command: String): Result<String>
}

class ShizukuCommandRunner : CommandRunner {
    override suspend fun execute(command: String): Result<String> = withContext(Dispatchers.IO) {
        if (!Shizuku.pingBinder()) {
            return@withContext Result.failure(Exception("Shizuku is not running"))
        }

        if (Shizuku.checkSelfPermission() != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            return@withContext Result.failure(Exception("Shizuku permission not granted"))
        }

        try {
            val process: ShizukuRemoteProcess = Shizuku.newProcess(arrayOf("sh", "-c", command), null, null)
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val errorReader = BufferedReader(InputStreamReader(process.errorStream))
            
            val output = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                output.append(line).append("\n")
            }

            val errorOutput = StringBuilder()
            while (errorReader.readLine().also { line = it } != null) {
                errorOutput.append(line).append("\n")
            }

            process.waitFor()

            if (process.exitValue() == 0) {
                Result.success(output.toString().trim())
            } else {
                Result.failure(Exception("Command failed with exit code ${process.exitValue()}: ${errorOutput.toString().trim()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
