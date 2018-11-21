package com.lai.opengl30demo.utils

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES11Ext
import android.opengl.GLES30
import android.opengl.GLUtils
import android.util.Log
import com.lai.opengl30demo.BuildConfig
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer
import javax.microedition.khronos.opengles.GL10


/**
 *
 * @author  Lai
 *
 * @time 2018/9/1 13:36
 * @describe describe
 *
 */
class OpenGlUtils {

    companion object {
        private const val TAG = "OpenGlUtils"

        //通过路径加载Assets中的文本内容
        private fun uRes(mRes: Resources, path: String): String? {
            val result = StringBuilder()
            try {
                val ass = mRes.assets.open(path)
                var ch: Int
                val buffer = ByteArray(1024)
                ch = ass.read(buffer)
                while (-1 != ch) {
                    result.append(String(buffer, 0, ch))
                    ch = ass.read(buffer)
                }
            } catch (e: Exception) {
                return null
            }
            return result.toString().replace("\\r\\n".toRegex(), "\n")
        }


        //创建GL程序
        fun uCreateGlProgram(vertexSource: String, fragmentSource: String, resources: Resources): Int {
            var program = GLES30.glCreateProgram()
            //当读取到的数据都不为空时才加载程序
            uRes(resources, vertexSource)?.let {
                uRes(resources, fragmentSource)?.run {
                    val vertex = uLoadShader(GLES30.GL_VERTEX_SHADER, it)
                    if (vertex == 0) return 0
                    val fragment = uLoadShader(GLES30.GL_FRAGMENT_SHADER, this)
                    if (fragment == 0) return 0
                    if (program != 0) {
                        GLES30.glAttachShader(program, vertex)
                        GLES30.glAttachShader(program, fragment)
                        GLES30.glLinkProgram(program)
                        val linkStatus = IntArray(1)
                        GLES30.glGetProgramiv(program, GLES30.GL_LINK_STATUS, linkStatus, 0)
                        if (linkStatus[0] != GLES30.GL_TRUE) {
                            glError(1, "Could not link program:" + GLES30.glGetProgramInfoLog(program))
                            GLES30.glDeleteProgram(program)
                            program = 0
                        }
                    }
                }
            }
            return program
        }

        private fun glError(code: Int, index: Any) {
            if (BuildConfig.DEBUG && code != 0) {
                Log.e(TAG, "glError:$code---$index")
            }
        }

        //加载shader
        private fun uLoadShader(shaderType: Int, source: String): Int {
            var shader = GLES30.glCreateShader(shaderType)
            if (0 != shader) {
                GLES30.glShaderSource(shader, source)
                GLES30.glCompileShader(shader)
                val compiled = IntArray(1)
                GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compiled, 0)
                if (compiled[0] == 0) {
                    glError(1, "Could not compile shader:$shaderType")
                    glError(1, "GLES30 Error:" + GLES30.glGetShaderInfoLog(shader))
                    GLES30.glDeleteShader(shader)
                    shader = 0
                }
            }
            return shader
        }

        /**
         * Buffer初始化
         */
        fun toBuffer(pos: FloatArray): FloatBuffer {
            val a = ByteBuffer.allocateDirect(pos.count() * 4)
            a.order(ByteOrder.nativeOrder())
            val mVerBuffer = a.asFloatBuffer()
            mVerBuffer.put(pos)
            mVerBuffer.position(0)
            return mVerBuffer
        }

        /**
         * 加载一个纹理对象到openGl
         */
        fun loadTexture(resources: Resources, resourceId: Int, textureObject: Int, textureNum: Int, textureType: Int): Int {
            val textureObjectIds = IntArray(1)
            //创建一个纹理
            GLES30.glGenTextures(1, textureObjectIds, 0)
            if (textureObjectIds[0] == 0) {
                Log.w(TAG, "创建纹理失败!")
            }

            //激活第几个纹理
            GLES30.glActiveTexture(GLES30.GL_TEXTURE0 + textureNum)
            //绑定纹理
            GLES30.glBindTexture(textureType, textureObjectIds[0])
            //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
            GLES30.glTexParameteri(textureType, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_NEAREST)
            //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
            GLES30.glTexParameteri(textureType, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
            //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES30.glTexParameteri(textureType, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE)
            //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES30.glTexParameteri(textureType, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE)

            if (textureType == GLES30.GL_TEXTURE_2D) {
                val options = BitmapFactory.Options()
                options.inScaled = false
                val bitmap = BitmapFactory.decodeResource(resources, resourceId, options)

                if (bitmap == null) {
                    Log.w(TAG, "载入位图失败")
                    GLES30.glDeleteTextures(1, textureObjectIds, 0)
                    return 0
                }
                //生成一个2D纹理
                GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0)
                bitmap.recycle()
            }
            //赋值到着色器
            GLES30.glUniform1i(textureObject, GLES30.GL_TEXTURE0 + textureNum)
            return textureObjectIds[0]
        }

        fun createOesTextureId(resources: Resources, resourceId: Int): Int {
            //textureObjectIds用于存储OpenGL生成纹理对象的ID，我们只需要一个纹理
            val textureObjectIds = IntArray(1)
            //1代表生成一个纹理
            GLES30.glGenTextures(1, textureObjectIds, 0)
            //判断是否生成成功
            if (textureObjectIds[0] == 0) {
                return 0
            }
            //加载纹理资源，解码成bitmap形式
            val options = BitmapFactory.Options()
            options.inScaled = false
            val bitmap = BitmapFactory.decodeResource(resources, resourceId, options)

            if (bitmap == null) {
                //删除指定的纹理对象
                GLES30.glDeleteTextures(1, textureObjectIds, 0)
                return 0
            }
            //第一个参数代表这是一个2D纹理，第二个参数就是OpenGL要绑定的纹理对象ID，也就是让OpenGL后面的纹理调用都使用此纹理对象
            GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureObjectIds[0])
            //设置纹理过滤参数，GL_TEXTURE_MIN_FILTER代表纹理缩写的情况，GL_LINEAR_MIPMAP_LINEAR代表缩小时使用三线性过滤的方式，至于过滤方式以后再详解
            GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR_MIPMAP_LINEAR)
            //GL_TEXTURE_MAG_FILTER代表纹理放大，GL_LINEAR代表双线性过滤
            GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
            //加载实际纹理图像数据到OpenGL ES的纹理对象中，这个函数是Android封装好的，可以直接加载bitmap格式，
            GLUtils.texImage2D(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0, bitmap, 0)
            //bitmap已经被加载到OpenGL了，所以bitmap可释放掉了，防止内存泄露
            bitmap.recycle()
            //返回OpenGL生成的纹理对象ID
            return textureObjectIds[0]
        }




        fun createTextureId(resources: Resources, resourceId: Int): Int {
            //textureObjectIds用于存储OpenGL生成纹理对象的ID，我们只需要一个纹理
            val textureObjectIds = IntArray(1)
            //1代表生成一个纹理
            GLES30.glGenTextures(1, textureObjectIds, 0)
            //判断是否生成成功
            if (textureObjectIds[0] == 0) {
                return 0
            }
            //加载纹理资源，解码成bitmap形式
            val options = BitmapFactory.Options()
            options.inScaled = false
            val bitmap = BitmapFactory.decodeResource(resources, resourceId, options)

            if (bitmap == null) {
                //删除指定的纹理对象
                GLES30.glDeleteTextures(1, textureObjectIds, 0)
                return 0
            }
            //第一个参数代表这是一个2D纹理，第二个参数就是OpenGL要绑定的纹理对象ID，也就是让OpenGL后面的纹理调用都使用此纹理对象
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureObjectIds[0])
            //设置纹理过滤参数，GL_TEXTURE_MIN_FILTER代表纹理缩写的情况，GL_LINEAR_MIPMAP_LINEAR代表缩小时使用三线性过滤的方式，至于过滤方式以后再详解
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR_MIPMAP_LINEAR)
            //GL_TEXTURE_MAG_FILTER代表纹理放大，GL_LINEAR代表双线性过滤
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
            //加载实际纹理图像数据到OpenGL ES的纹理对象中，这个函数是Android封装好的，可以直接加载bitmap格式，
            GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0)
            //bitmap已经被加载到OpenGL了，所以bitmap可释放掉了，防止内存泄露
            bitmap.recycle()
            //我们为纹理生成MIP贴图，提高渲染性能，但是可占用较多的内存
            GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D)
            //现在OpenGL已经完成了纹理的加载，不需要再绑定此纹理了，后面使用此纹理时通过纹理对象的ID即可
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0)
            //返回OpenGL生成的纹理对象ID
            return textureObjectIds[0]
        }

        fun createTextureId(resources: Resources, resourceId: Int,i:Boolean): Int {
            //textureObjectIds用于存储OpenGL生成纹理对象的ID，我们只需要一个纹理
            val textureObjectIds = IntArray(1)
            //1代表生成一个纹理
            GLES30.glGenTextures(1, textureObjectIds, 0)
            //判断是否生成成功
            if (textureObjectIds[0] == 0) {
                return 0
            }
            //加载纹理资源，解码成bitmap形式
            val options = BitmapFactory.Options()
            options.inScaled = false
            val bitmap = BitmapFactory.decodeResource(resources, resourceId, options)
            if (bitmap == null) {
                //删除指定的纹理对象
                GLES30.glDeleteTextures(1, textureObjectIds, 0)
                return 0
            }
            //第一个参数代表这是一个2D纹理，第二个参数就是OpenGL要绑定的纹理对象ID，也就是让OpenGL后面的纹理调用都使用此纹理对象
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureObjectIds[0])
            //设置纹理过滤参数，GL_TEXTURE_MIN_FILTER代表纹理缩写的情况，GL_LINEAR_MIPMAP_LINEAR代表缩小时使用三线性过滤的方式，至于过滤方式以后再详解
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR_MIPMAP_LINEAR)
            //GL_TEXTURE_MAG_FILTER代表纹理放大，GL_LINEAR代表双线性过滤
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
            //加载实际纹理图像数据到OpenGL ES的纹理对象中，这个函数是Android封装好的，可以直接加载bitmap格式，
            GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0)
            //bitmap已经被加载到OpenGL了，所以bitmap可释放掉了，防止内存泄露
            bitmap.recycle()
            //我们为纹理生成MIP贴图，提高渲染性能，但是可占用较多的内存
            GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D)
            //现在OpenGL已经完成了纹理的加载，不需要再绑定此纹理了，后面使用此纹理时通过纹理对象的ID即可
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0)
            //返回OpenGL生成的纹理对象ID
            return textureObjectIds[0]
        }

        fun convertToBitmap(mWidth: Int, mHeight: Int, eglContext: GL10): Bitmap {
            val iat = IntArray(mWidth * mHeight)
            val ib = IntBuffer.allocate(mWidth * mHeight)
            eglContext.glReadPixels(0, 0, mWidth, mHeight, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE,
                    ib)
            val ia = ib.array()

            // Convert upside down mirror-reversed image to right-side up normal
            // image.
            for (i in 0 until mHeight) {
                System.arraycopy(ia, i * mWidth, iat, (mHeight - i - 1) * mWidth, mWidth)
            }
             val bitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888)
            bitmap.copyPixelsFromBuffer(IntBuffer.wrap(iat))
            return bitmap
        }

        fun SavePixels(x: Int, y: Int, w: Int, h: Int, gl: GL10): Bitmap {
            val b = IntArray(w * (y + h))
            val bt = IntArray(w * h)
            val ib = IntBuffer.wrap(b)
            ib.position(0)
            gl.glReadPixels(x, 0, w, y + h, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, ib)

            var i = 0
            var k = 0
            while (i < h) {//remember, that OpenGL bitmap is incompatible with Android bitmap
                //and so, some correction need.
                for (j in 0 until w) {
                    val pix = b[i * w + j]
                    val pb = pix shr 16 and 0xff
                    val pr = pix shl 16 and 0x00ff0000
                    val pix1 = pix and -0xff0100 or pr or pb
                    bt[(h - k - 1) * w + j] = pix1
                }
                i++
                k++
            }

            return Bitmap.createBitmap(bt, w, h, Bitmap.Config.ARGB_8888)
        }

    }


}