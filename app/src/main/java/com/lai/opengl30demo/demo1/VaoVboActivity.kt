package com.lai.opengl30demo.demo1

import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.lai.opengl30demo.R
import com.lai.opengl30demo.utils.OpenGlUtils
import com.lai.opengl30demo.utils.VOBUtils
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class VaoVboActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val glSurfaceView = findViewById<GLSurfaceView>(R.id.gl_surface)
        glSurfaceView.setEGLContextClientVersion(3)
        //设置渲染
        glSurfaceView.setRenderer(TriangleRenderer())
        //创建和调用requestRender()时才会刷新
        glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
    }


    /**
     * onDrawFrame：绘制时调用
     * onSurfaceChanged：改变大小时调用
     * onSurfaceCreated：创建时调用
     */
    inner class TriangleRenderer : GLSurfaceView.Renderer {

        //顶点坐标
        private val vertices = floatArrayOf(-0.5f, -0.5f, 0.0f, 0.5f, -0.5f, 0.0f, 0.0f, 0.5f, 0.0f)

        private val verticesBuffer: FloatBuffer

        lateinit var vobUtils: VOBUtils

        var program = 0

        init {
            verticesBuffer = OpenGlUtils.toBuffer(vertices)
        }


        override fun onDrawFrame(gl: GL10?) {

            // render
            // ------
            GLES30.glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
            GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)

            //将程序加入到OpenGLES2.0环境
            GLES30.glUseProgram(program)

            vobUtils.bindVao()

            //绘制三角形
            GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 3)
            //禁止顶点数组的句柄
            //GLES30.glDisableVertexAttribArray(0)

            val endTime = System.currentTimeMillis() //获取结束时间
            vobUtils.relese()
        }

        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
            GLES30.glViewport(0, 0, width, height)
        }

        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
            program = OpenGlUtils.uCreateGlProgram("triangle_vertex.glsl", "triangle_fragment.glsl", resources)
            vobUtils = VOBUtils(verticesBuffer)
            vobUtils.createVBOAndVAO()
            //启用三角形顶点的句柄
            //GLES30.glEnableVertexAttribArray(0)
            //准备三角形的坐标数据
            //GLES30.glVertexAttribPointer(0, 3, GLES20.GL_FLOAT, false, 0, verticesBuffer)
        }


    }

}
