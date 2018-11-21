package com.lai.opengl30demo.utils

import android.annotation.TargetApi
import android.opengl.GLES20
import android.opengl.GLES30
import android.os.Build
import java.nio.FloatBuffer

/**
 *
 * @author  Lai
 *
 * @time 2018/10/3 16:47
 * @describe describe
 *
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class VOBUtils(private val verticesBuffer: FloatBuffer) {

    //vbo id
    private val vboId = IntArray(1)
    //vao id
    private val vaoId = IntArray(1)

    fun getVobId(): Int {
        return vboId[0]
    }

    //顶点缓冲数据
    fun createVBOAndVAO() {
        //生成VAO
        GLES30.glGenVertexArrays(1, vaoId, 0)
        //绑定VAO
        GLES30.glBindVertexArray(vaoId[0])
        //生成VBO
        GLES30.glGenBuffers(1, vboId, 0)
        //绑定VBO
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vboId[0])
        //把顶点数组复制到缓冲中供OpenGL使用 ， 数据复制到当前绑定VBO
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, verticesBuffer.capacity() * 4, verticesBuffer, GLES30.GL_STATIC_DRAW)//把数据存储到GPU中
        //启用三角形顶点的句柄
        GLES30.glEnableVertexAttribArray(0)
        //准备三角形的坐标数据，通知OpenGL如何解释这些顶点数据
        GLES30.glVertexAttribPointer(0, 3, GLES20.GL_FLOAT, false, 0, 0)
        //现在不使用这个缓冲区
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, GLES30.GL_NONE)
        //现在不使用这个VAO
        GLES30.glBindVertexArray(GLES30.GL_NONE)
    }

    //用的时候
    fun bindVao() {
        //GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vboId[0])//绑定存在的VBO
        GLES30.glBindVertexArray(vaoId[0])//绑定VAO
    }

    fun relese() {
        //现在不使用这个缓冲区
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, GLES30.GL_NONE)
        //现在不使用这个VAO
        GLES30.glBindVertexArray(GLES30.GL_NONE)
        //删除
        GLES30.glDeleteVertexArrays(1, vaoId, 0)
        GLES30.glDeleteBuffers(1, vboId, 0)
    }

}