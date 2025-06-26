package com.python

import android.content.Context
import android.util.Log
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.python.data.LogManager
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PythonRunner {
    private const val TAG = "PythonRunner"


    //通过传code运行
    fun run(context: Context, code: String, filename: String): String {
        fun writeLog(text: String) {
            LogManager.append(text)
        }


        val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        writeLog("\n------------------------------------------\n[$time] 文件 $filename 开始运行\n")


        val output = try {
            val py = Python.getInstance()
            val sys = py.getModule("sys")
            val io = py.getModule("io")
            val outputStream = io.callAttr("StringIO")
            sys["stdout"] = outputStream
            sys["stderr"] = outputStream

            val mainModule = py.getModule("__main__")
            val globals = mainModule.get("__dict__")

            py.getBuiltins().callAttr("exec", code, globals, globals)

            outputStream.callAttr("getvalue").toString().also {
                Log.d(TAG, "Python执行结果:\n$it")
                writeLog("运行结果:\n$it\n")
            }
        } catch (e: Exception) {
            val err = "错误：${e.message}"
            Log.e(TAG, "Python执行异常", e)
            writeLog("$err\n")
            err
        } finally {
            val endTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            writeLog("[$endTime] 文件 $filename 运行结束\n\n")
        }

        return output
    }

    // 方案二：读取文件内容，调用 run 方法执行
    fun runFile(context: Context, file: File): String {
        val code = try {
            file.readText()
        } catch (e: Exception) {
            return "读取文件失败：${e.message}"
        }
        return run(context, code, file.name)
    }

    // 方案三：用 runpy.run_path 直接运行文件
    fun runFileWithRunpy(context: Context, file: File): String {


        fun writeLog(text: String) {
            LogManager.append(text)
        }


        val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        writeLog("\n------------------------------------------\n[$time] 文件 ${file.name} 开始运行\n")

        val output = try {
            val py = Python.getInstance()
            val sys = py.getModule("sys")
            val io = py.getModule("io")
            val outputStream = io.callAttr("StringIO")
            sys["stdout"] = outputStream
            sys["stderr"] = outputStream

            val runpy = py.getModule("runpy")

            runpy.callAttr("run_path", file.absolutePath)

            outputStream.callAttr("getvalue").toString().also {
                Log.d(TAG, "Python执行结果:\n$it")
                writeLog("运行结果:\n$it\n")
            }
        } catch (e: Exception) {
            val err = "错误：${e.message}"
            Log.e(TAG, "Python执行异常", e)
            writeLog("$err\n")
            err
        } finally {
            val endTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            writeLog("[$endTime] 文件 ${file.name} 运行结束\n\n")
        }

        return output
    }


}

