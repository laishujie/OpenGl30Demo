#version 300 es
precision mediump float;

out vec4 fragColor;
//从顶点着色器传来的输入变量（名称相同、类型相同）
in vec4 color;
void main()
{
    //将颜色输出
    fragColor = color;
}