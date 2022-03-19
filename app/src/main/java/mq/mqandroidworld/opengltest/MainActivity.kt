package mq.mqandroidworld.opengltest

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import mq.mqandroidworld.opengltest.ui.theme.OpenglTestTheme
import java.lang.StringBuilder
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val glSurfaceView = GLSurfaceView(this)
        glSurfaceView.setEGLContextClientVersion(2)
        glSurfaceView.setRenderer(MyRender())
        setContent {
            OpenglTestTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Greeting(name = "Android", glSurfaceView = glSurfaceView)
                }
            }
        }
    }
}
class MyRender : GLSurfaceView.Renderer {
    private lateinit var mTriangle: Triangle
    private lateinit var mCube:Cube
    private lateinit var mLighting: Lighting
    private lateinit var mCube2: Cube2
    // vPMatrix is an abbreviation for "Model View Projection Matrix" 줄임말
    private val vPMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val mvmMatrix = FloatArray(16)
    private var ratios=0f;

    override fun onSurfaceCreated(unused: GL10?, config: EGLConfig?) {
        // 배경화면 색 설정
        GLES20.glClearColor(0f,0f,0f,1f)

        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glDepthFunc(GLES20.GL_LEQUAL)
        GLES20.glDepthMask(true)
        //삼각형 초기화
        mTriangle = Triangle()

        mCube = Cube()

        mLighting = Lighting()

        mCube2 = Cube2()
    }

    override fun onSurfaceChanged(unused: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0,0, width, height)

        val ratio:Float = width.toFloat() / height.toFloat()
        ratios = ratio
        //onDrawFrame에서 오브젝트의 좌표를 설정할 때 사용된다.
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 50f)
    }

    val rotationMatrix = FloatArray(16)

    // 불빛
    private val mLightPosInModelSpace = floatArrayOf(0f, 0f, 0f, 1f)
    private val mLightModelMatrix = FloatArray(16)
    private val mLightPosInWorldSpace = FloatArray(4)
    private val mLightPosInEyeSpace = FloatArray(4)

    override fun onDrawFrame(p0: GL10?) {
        // 다시 그릴 때 호출된다.
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        // 카메라 위치 설정
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 20f, 0f, 0f, 0f, 0f,1f,0f)

        // projection 과 transformation 계산하기
        Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)


        val scratch = FloatArray(16)

        // Create a rotation transformation for the triangle
        val time = SystemClock.uptimeMillis() % 4000L
        val angle = 0.090f * time.toInt()
        Matrix.setRotateM(rotationMatrix, 0, angle, 1f, 0f, 0.0f)
        Log.i("andgle1234",angle.toString())
        // Combine the rotation matrix with the projection and camera view
        // Note that the vPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.
        Matrix.multiplyMM(scratch, 0, vPMatrix, 0, rotationMatrix, 0)

        mTriangle.draw(scratch)

        val translateM = FloatArray(16)


        Matrix.setIdentityM(translateM,0)
        Matrix.translateM(translateM,0,0.5f,0f,-1f)
        Matrix.multiplyMM(translateM, 0, translateM, 0, scratch, 0)
        mTriangle.draw(translateM)

        Matrix.setIdentityM(translateM,0)
        Matrix.translateM(translateM,0,0f,0.5f,0f)
        Matrix.multiplyMM(translateM, 0, translateM, 0, vPMatrix, 0)
        mTriangle.draw(translateM)

        Matrix.setIdentityM(translateM,0)
        Matrix.translateM(translateM,0,0f,-0.5f,0f)
        Matrix.multiplyMM(translateM, 0, translateM, 0, vPMatrix, 0)
        mTriangle.draw(translateM)

        Matrix.setIdentityM(translateM,0)
        Matrix.translateM(translateM,0,-0.5f,0f,0f)
        Matrix.multiplyMM(translateM, 0, translateM, 0, vPMatrix, 0)
        mTriangle.draw(translateM)


        // cube draw
        val x = 0f
        val y = 0f
        val z = 3f

        Matrix.setIdentityM(translateM,0)
//        Matrix.translateM(
//            translateM,
//            0,
//            if(ratios > 1f) x/ratios else x,
//            if(ratios < 1f) y*ratios else y,
//            z)
        Log.i("asdf1234", translateM.toStrings())
//        Matrix.multiplyMM(translateM, 0, translateM, 0, projectionMatrix, 0)

        // (0,0,0)를 중심으로 떨어진 거리만큼 공전을 한다. 중심좌표는 수정가능.
        Matrix.multiplyMM(translateM, 0, translateM, 0, scratch, 0)
        Matrix.translateM(translateM, 0, x,y,z)
        //mCube.draw(translateM)


        // 옮긴후 제자리에서 회전한다.
        Matrix.setIdentityM(translateM,0)
        Matrix.multiplyMM(translateM, 0, translateM, 0, vPMatrix, 0)
        Matrix.translateM(translateM, 0, x,y,z)
        Matrix.multiplyMM(translateM, 0, translateM, 0, rotationMatrix, 0)
        //mCube.draw(translateM)



        //불빛 
        val lights = FloatArray(16)
        Matrix.setIdentityM(mLightModelMatrix,0)
        Matrix.translateM(mLightModelMatrix,0, 0f, 0f, 0f)
        Matrix.rotateM(mLightModelMatrix, 0, angle, 1f, 1f, 1f)
        Matrix.translateM(mLightModelMatrix,0, 0f, 0f, 5f)
        
        // 이거 아직 사용 안함 불빛만 회전하게 만듬
        Matrix.multiplyMV(mLightPosInWorldSpace, 0, mLightModelMatrix, 0, mLightPosInModelSpace, 0)
        Matrix.multiplyMV(mLightPosInEyeSpace, 0, viewMatrix, 0, mLightPosInWorldSpace, 0)

        Matrix.multiplyMM(lights, 0, viewMatrix, 0, mLightModelMatrix, 0)
        Matrix.multiplyMM(lights, 0, projectionMatrix, 0, lights, 0)
        mLighting.draw(lights, mLightPosInModelSpace = mLightPosInModelSpace)


        // 큐브v2
        val mvpMatrix= FloatArray(16)
        Matrix.setIdentityM(translateM, 0)
        Matrix.translateM(translateM,0, 0f, 0f, 0f)
        Matrix.rotateM(translateM, 0, -angle, 1f, 1f, 0f)
        Matrix.multiplyMM(mvmMatrix, 0, viewMatrix, 0, translateM, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvmMatrix, 0)

        mCube2.draw(mvpMatrix = mvpMatrix, mvmMatrix = mvmMatrix, mLightPosInEyeSpace = mLightPosInEyeSpace)
//        mCube.draw(mvpMatrix = mvpMatrix)
    }
}
fun FloatArray.toStrings():String{
    val stringBuilder = StringBuilder()
    forEach {
        stringBuilder.append(it)
        stringBuilder.append(' ')
    }
    return  stringBuilder.toString()
}

fun loadShader(type: Int, shaderCode: String): Int{
    // vertex shader type or fragment shader type을 만든다.
    return GLES20.glCreateShader(type).also { shader ->
        // shader을 위한 소스코드를 추가하고 컴파일 한다.
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)
    }
}
class Triangle {
    companion object {
        // 좌표당 꼭지점 갯수
        const val COORDINATE_PER_VERTEX = 3
        var triangleCoordinate = floatArrayOf(
            0.0f, 0.622008459f, 0.0f,      // top
            -0.5f, -0.311004243f, 0.0f,    // bottom left
            0.5f, -0.311004243f, 0.0f      // bottom right
        )
    }
    private var vPMatrixHandle = 0
    private val vertexShaderCode =
        """
            uniform mat4 uMVPMatrix;
            attribute vec4 vPosition;
            void main() {
                gl_Position = uMVPMatrix * vPosition;
            }
        """.trimIndent()
//    // This matrix member variable provides a hook to manipulate
//        // the coordinates of the objects that use this vertex shader
//                "uniform mat4 uMVPMatrix;" +
//                "attribute vec4 vPosition;" +
//                "void main() {" +
//                // the matrix must be included as a modifier of gl_Position
//                // Note that the uMVPMatrix factor *must be first* in order
//                // for the matrix multiplication product to be correct.
//                "  gl_Position = uMVPMatrix * vPosition;" +
//                "}"

    private val fragmentShaderCode =
        """
            precision mediump float;
            uniform vec4 vColor;
            void main() {
                gl_FragColor = vColor;
            }
        """.trimIndent()
//        "precision mediump float;" +
//                "uniform vec4 vColor;" +
//                "void main() {" +
//                "  gl_FragColor = vColor;" +
//                "}"

    val color = floatArrayOf(0.63671875f, 0.76953125f, 0.22265625f, 1.0f)   // 색상 지정
    private var vertexBuffer: FloatBuffer =
        // bytebuffer 직접 할당하기
        ByteBuffer.allocateDirect(triangleCoordinate.size * 4).run {
            // 기기는 nativer byte 순서를 사용한다.
            order(ByteOrder.nativeOrder())

            // bytebuffer로 부터 floatBuffer을 만들어낸다.
            asFloatBuffer().apply {
                // floatbuffer을 위한 좌표를 추가한다.
                put(triangleCoordinate)
                // 첫번째 좌표를 읽기 버퍼를 설정한다.
                position(0)
            }
        }

    private var mProgram: Int

    init {
        val vertexShader: Int = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader: Int = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        // 텅 비어있는 opengl es program 만들기
        mProgram = GLES20.glCreateProgram().also {
            // 프로그램에 vertex shader 추가하기
            GLES20.glAttachShader(it, vertexShader)
            // 프로그램에 fragment shader 추가하기
            GLES20.glAttachShader(it, fragmentShader)

            // opengl es 프로그램 실행할 수 있게 만들기
            GLES20.glLinkProgram(it)
        }
    }


    private var positionHandle: Int = 0
    private var mColorHandle: Int = 0

    private val vertexCount: Int = triangleCoordinate.size / COORDINATE_PER_VERTEX
    private val vertexStride: Int = COORDINATE_PER_VERTEX * 4   // 한 꼭지점(vertex)당 4byte
    fun draw(mvpMatrix: FloatArray){
        // opengl ES 환경에 프로그램 추가하기
        GLES20.glUseProgram(mProgram)

        //
        positionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition").also {
            // 정점 추가. enable
            GLES20.glEnableVertexAttribArray(it)

            // 삼각형 좌표 데이터 준비하기
            GLES20.glVertexAttribPointer(
                it,
                COORDINATE_PER_VERTEX,
                GLES20.GL_FLOAT,
                false,
                vertexStride,
                vertexBuffer
            )

            // fragment shader의 vColor 멤버 다루기
            mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor").also { colorHandle ->
                // 삼각형 그릴때 색 설정
                GLES20.glUniform4fv(colorHandle, 1, color, 0)
            }

            vPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix")
            GLES20.glUniformMatrix4fv(vPMatrixHandle, 1, false, mvpMatrix, 0)

            // 삼각형 그리기
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount)

            // disable vertex array// diable 하기
            GLES20.glDisableVertexAttribArray(it)
        }
    }
}

class Cube {
    companion object {
        const val COORDINATE_PER_VERTEX = 3
        var cubeCoordinates = floatArrayOf(
            // FRONT
            -1.0f, -1.0f,  1.0f,  // 0. left-bottom-front
            1.0f, -1.0f,  1.0f,  // 1. right-bottom-front
            -1.0f,  1.0f,  1.0f,  // 2. left-top-front
            -1.0f,  1.0f,  1.0f,  // 2. left-top-front
            1.0f, -1.0f,  1.0f,  // 1. right-bottom-front
            1.0f,  1.0f,  1.0f,  // 3. right-top-front
            // BACK
            1.0f, -1.0f, -1.0f,  // 6. right-bottom-back
            1.0f,  1.0f, -1.0f,  // 7. right-top-back
            -1.0f, -1.0f, -1.0f,  // 4. left-bottom-back
            -1.0f, -1.0f, -1.0f,  // 4. left-bottom-back
            -1.0f,  1.0f, -1.0f,  // 5. left-top-back
            1.0f,  1.0f, -1.0f,  // 7. right-top-back
            // LEFT
            -1.0f, -1.0f, -1.0f,  // 4. left-bottom-back
            -1.0f, -1.0f,  1.0f,  // 0. left-bottom-front
            -1.0f,  1.0f,  1.0f,  // 2. left-top-front
            -1.0f,  1.0f,  1.0f,  // 2. left-top-front
            -1.0f,  1.0f, -1.0f,  // 5. left-top-back
            -1.0f, -1.0f, -1.0f,  // 4. left-bottom-back
            // RIGHT
            1.0f, -1.0f,  1.0f,  // 1. right-bottom-front
            1.0f, -1.0f, -1.0f,  // 6. right-bottom-back
            1.0f,  1.0f, -1.0f,  // 7. right-top-back
            1.0f,  1.0f, -1.0f,  // 7. right-top-back
            1.0f,  1.0f,  1.0f,  // 3. right-top-front
            1.0f, -1.0f,  1.0f,  // 1. right-bottom-front
            // TOP
            -1.0f,  1.0f,  1.0f,  // 2. left-top-front
            1.0f,  1.0f,  1.0f,  // 3. right-top-front
            1.0f,  1.0f, -1.0f,  // 7. right-top-back
            1.0f,  1.0f, -1.0f,  // 7. right-top-back
            -1.0f,  1.0f, -1.0f,  // 5. left-top-back
            -1.0f,  1.0f,  1.0f,  // 2. left-top-front
            // BOTTOM
            -1.0f, -1.0f, -1.0f,  // 4. left-bottom-back
            1.0f, -1.0f, -1.0f,  // 6. right-bottom-back
            1.0f, -1.0f,  1.0f,   // 1. right-bottom-front
            1.0f, -1.0f,  1.0f,   // 1. right-bottom-front
            -1.0f, -1.0f,  1.0f,  // 0. left-bottom-front
            -1.0f, -1.0f, -1.0f  // 4. left-bottom-back
        )
    }
    private val vertexShaderCode =
        """
            uniform mat4 uMVPMatrix;
            attribute vec4 vPosition;
            void main() {
                gl_Position = uMVPMatrix * vPosition;
            }
        """.trimIndent()

//    // This matrix member variable provides a hook to manipulate
//        // the coordinates of the objects that use this vertex shader
//        "uniform mat4 uMVPMatrix;" +
//        "attribute vec4 vPosition;" +
//        "void main() {" +
//        // the matrix must be included as a modifier of gl_Position
//        // Note that the uMVPMatrix factor *must be first* in order
//        // for the matrix multiplication product to be correct.
//        "  gl_Position = uMVPMatrix * vPosition;" +
//        "}"

    private val fragmentShaderCode =
        """
            precision mediump float;
            uniform vec4 vColor;
            void main() {
                gl_FragColor = vColor;
            }
        """.trimIndent()
//        "precision mediump float;" +
//                "uniform vec4 vColor;" +
//                "void main() {" +
//                "  gl_FragColor = vColor;" +
//                "}"

    val color = floatArrayOf(0.5f, 0.5f, 0.5f, 1.0f)   // 색상 지정

    private var vertexBuffer: FloatBuffer =
        // bytebuffer 직접 할당하기
        ByteBuffer.allocateDirect(Cube.cubeCoordinates.size * 4).run {
            // 기기는 nativer byte 순서를 사용한다.
            order(ByteOrder.nativeOrder())
            // bytebuffer로 부터 floatBuffer을 만들어낸다.
            asFloatBuffer().apply {
                // floatbuffer을 위한 좌표를 추가한다.
                put(Cube.cubeCoordinates)
                // 첫번째 좌표를 읽기 버퍼를 설정한다.
                position(0)
            }
        }
    private var mProgram: Int
    init {
        val vertexShader: Int = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader: Int = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        // 텅 비어있는 opengl es program 만들기
        mProgram = GLES20.glCreateProgram().also {
            // 프로그램에 vertex shader 추가하기
            GLES20.glAttachShader(it, vertexShader)
            // 프로그램에 fragment shader 추가하기
            GLES20.glAttachShader(it, fragmentShader)

            // opengl es 프로그램 실행할 수 있게 만들기
            GLES20.glLinkProgram(it)
        }
    }
    private var positionHandle: Int = 0
    private var vPMatrixHandle = 0
    private var mColorHandle: Int = 0

    private val vertexCount: Int = Cube.cubeCoordinates.size / Cube.COORDINATE_PER_VERTEX
    private val vertexStride: Int = Cube.COORDINATE_PER_VERTEX * 4   // 한 꼭지점(vertex)당 4byte
    fun draw(mvpMatrix: FloatArray){
        // opengl ES 환경에 프로그램 추가하기
        GLES20.glUseProgram(mProgram)

        //
        positionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition").also {
            // 정점 추가. enable
            GLES20.glEnableVertexAttribArray(it)

            // 큐브 좌표 데이터 준비하기 (만들기 위한것)
            GLES20.glVertexAttribPointer(
                it,
                Cube.COORDINATE_PER_VERTEX,
                GLES20.GL_FLOAT,
                false,
                vertexStride,
                vertexBuffer
            )

            // fragment shader의 vColor 멤버 다루기
            mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor").also { colorHandle ->
                // 삼각형 그릴때 색 설정
                GLES20.glUniform4fv(colorHandle, 1, color, 0)
            }

            vPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix")
            GLES20.glUniformMatrix4fv(vPMatrixHandle, 1, false, mvpMatrix, 0)

            // 삼각형 그리기
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount)

            // disable vertex array // diable 하기
            GLES20.glDisableVertexAttribArray(it)
        }
    }
}

class Cube2 {
    companion object {
        const val COORDINATE_PER_VERTEX = 3
        const val NORMAL_DATA_SIZE = 3
        const val COLOR_DATA_SIZE = 4
        var cubeCoordinates = floatArrayOf(
//            // FRONT
//            -1.0f, -1.0f,  1.0f,  // 0. left-bottom-front
//            1.0f, -1.0f,  1.0f,  // 1. right-bottom-front
//            -1.0f,  1.0f,  1.0f,  // 2. left-top-front
//            -1.0f,  1.0f,  1.0f,  // 2. left-top-front
//            1.0f, -1.0f,  1.0f,  // 1. right-bottom-front
//            1.0f,  1.0f,  1.0f,  // 3. right-top-front
//            // BACK
//            1.0f, -1.0f, -1.0f,  // 6. right-bottom-back
//            1.0f,  1.0f, -1.0f,  // 7. right-top-back
//            -1.0f, -1.0f, -1.0f,  // 4. left-bottom-back
//            -1.0f, -1.0f, -1.0f,  // 4. left-bottom-back
//            -1.0f,  1.0f, -1.0f,  // 5. left-top-back
//            1.0f,  1.0f, -1.0f,  // 7. right-top-back
//            // LEFT
//            -1.0f, -1.0f, -1.0f,  // 4. left-bottom-back
//            -1.0f, -1.0f,  1.0f,  // 0. left-bottom-front
//            -1.0f,  1.0f,  1.0f,  // 2. left-top-front
//            -1.0f,  1.0f,  1.0f,  // 2. left-top-front
//            -1.0f,  1.0f, -1.0f,  // 5. left-top-back
//            -1.0f, -1.0f, -1.0f,  // 4. left-bottom-back
//            // RIGHT
//            1.0f, -1.0f,  1.0f,  // 1. right-bottom-front
//            1.0f, -1.0f, -1.0f,  // 6. right-bottom-back
//            1.0f,  1.0f, -1.0f,  // 7. right-top-back
//            1.0f,  1.0f, -1.0f,  // 7. right-top-back
//            1.0f,  1.0f,  1.0f,  // 3. right-top-front
//            1.0f, -1.0f,  1.0f,  // 1. right-bottom-front
//            // TOP
//            -1.0f,  1.0f,  1.0f,  // 2. left-top-front
//            1.0f,  1.0f,  1.0f,  // 3. right-top-front
//            1.0f,  1.0f, -1.0f,  // 7. right-top-back
//            1.0f,  1.0f, -1.0f,  // 7. right-top-back
//            -1.0f,  1.0f, -1.0f,  // 5. left-top-back
//            -1.0f,  1.0f,  1.0f,  // 2. left-top-front
//            // BOTTOM
//            -1.0f, -1.0f, -1.0f,  // 4. left-bottom-back
//            1.0f, -1.0f, -1.0f,  // 6. right-bottom-back
//            1.0f, -1.0f,  1.0f,   // 1. right-bottom-front
//            1.0f, -1.0f,  1.0f,   // 1. right-bottom-front
//            -1.0f, -1.0f,  1.0f,  // 0. left-bottom-front
//            -1.0f, -1.0f, -1.0f  // 4. left-bottom-back
            // In OpenGL counter-clockwise winding is default. This means that when we look at a triangle,
            // if the points are counter-clockwise we are looking at the "front". If not we are looking at
            // the back. OpenGL has an optimization where all back-facing triangles are culled, since they
            // usually represent the backside of an object and aren't visible anyways.

            // Front face
            -1.0f, 1.0f, 1.0f,
            -1.0f, -1.0f, 1.0f,
            1.0f, 1.0f, 1.0f,
            -1.0f, -1.0f, 1.0f,
            1.0f, -1.0f, 1.0f,
            1.0f, 1.0f, 1.0f,

            // Right face
            1.0f, 1.0f, 1.0f,
            1.0f, -1.0f, 1.0f,
            1.0f, 1.0f, -1.0f,
            1.0f, -1.0f, 1.0f,
            1.0f, -1.0f, -1.0f,
            1.0f, 1.0f, -1.0f,

            // Back face
            1.0f, 1.0f, -1.0f,
            1.0f, -1.0f, -1.0f,
            -1.0f, 1.0f, -1.0f,
            1.0f, -1.0f, -1.0f,
            -1.0f, -1.0f, -1.0f,
            -1.0f, 1.0f, -1.0f,

            // Left face
            -1.0f, 1.0f, -1.0f,
            -1.0f, -1.0f, -1.0f,
            -1.0f, 1.0f, 1.0f,
            -1.0f, -1.0f, -1.0f,
            -1.0f, -1.0f, 1.0f,
            -1.0f, 1.0f, 1.0f,

            // Top face
            -1.0f, 1.0f, -1.0f,
            -1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, -1.0f,
            -1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, -1.0f,

            // Bottom face
            1.0f, -1.0f, -1.0f,
            1.0f, -1.0f, 1.0f,
            -1.0f, -1.0f, -1.0f,
            1.0f, -1.0f, 1.0f,
            -1.0f, -1.0f, 1.0f,
            -1.0f, -1.0f, -1.0f,
        )
        var cubeNormalData = floatArrayOf(
            // Front face
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,

            // Right face
            1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,

            // Back face
            0.0f, 0.0f, -1.0f,
            0.0f, 0.0f, -1.0f,
            0.0f, 0.0f, -1.0f,
            0.0f, 0.0f, -1.0f,
            0.0f, 0.0f, -1.0f,
            0.0f, 0.0f, -1.0f,

            // Left face
            -1.0f, 0.0f, 0.0f,
            -1.0f, 0.0f, 0.0f,
            -1.0f, 0.0f, 0.0f,
            -1.0f, 0.0f, 0.0f,
            -1.0f, 0.0f, 0.0f,
            -1.0f, 0.0f, 0.0f,

            // Top face
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,

            // Bottom face
            0.0f, -1.0f, 0.0f,
            0.0f, -1.0f, 0.0f,
            0.0f, -1.0f, 0.0f,
            0.0f, -1.0f, 0.0f,
            0.0f, -1.0f, 0.0f,
            0.0f, -1.0f, 0.0f
        )
        var cubeColorData = floatArrayOf(
            // Front face (red)
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,

            // Right face (green)
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,

            // Back face (blue)
            0.0f, 0.0f, 1.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,

            // Left face (yellow)
            1.0f, 1.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 0.0f, 1.0f,

            // Top face (cyan)
            0.0f, 1.0f, 1.0f, 1.0f,
            0.0f, 1.0f, 1.0f, 1.0f,
            0.0f, 1.0f, 1.0f, 1.0f,
            0.0f, 1.0f, 1.0f, 1.0f,
            0.0f, 1.0f, 1.0f, 1.0f,
            0.0f, 1.0f, 1.0f, 1.0f,

            // Bottom face (magenta)
            1.0f, 0.0f, 1.0f, 1.0f,
            1.0f, 0.0f, 1.0f, 1.0f,
            1.0f, 0.0f, 1.0f, 1.0f,
            1.0f, 0.0f, 1.0f, 1.0f,
            1.0f, 0.0f, 1.0f, 1.0f,
            1.0f, 0.0f, 1.0f, 1.0f
        )
    }
    private val vertexShaderCode =
        """
            uniform mat4 u_MVPMatrix;      // A constant representing the combined model/view/projection matrix.
            uniform mat4 u_MVMatrix;       // A constant representing the combined model/view matrix.
             
            attribute vec4 a_Position;     // Per-vertex position information we will pass in.
            attribute vec4 a_Color;        // Per-vertex color information we will pass in.
            attribute vec3 a_Normal;       // Per-vertex normal information we will pass in.
             
            varying vec3 v_Position;       // This will be passed into the fragment shader.
            varying vec4 v_Color;          // This will be passed into the fragment shader.
            varying vec3 v_Normal;         // This will be passed into the fragment shader.
             
            // The entry point for our vertex shader.
            void main()
            {
                // Transform the vertex into eye space.
                v_Position = vec3(u_MVMatrix * a_Position);
             
                // Pass through the color.
                v_Color = a_Color;
             
                // Transform the normal's orientation into eye space.
                v_Normal = vec3(u_MVMatrix * vec4(a_Normal, 0.0));
             
                // gl_Position is a special variable used to store the final position.
                // Multiply the vertex by the matrix to get the final point in normalized screen coordinates.
                gl_Position = u_MVPMatrix * a_Position;
            }
        """.trimIndent()

    private val fragmentShaderCode =
        """
            precision mediump float;       // Set the default precision to medium. We don't need as high of a
                               // precision in the fragment shader.
            uniform vec3 u_LightPos;       // The position of the light in eye space.
            uniform vec4 u_Color;           // 단색
             
            varying vec3 v_Position;       // Interpolated position for this fragment.
            varying vec4 v_Color;          // This is the color from the vertex shader interpolated across the
                                           // triangle per fragment.
            varying vec3 v_Normal;         // Interpolated normal for this fragment.
             
            // The entry point for our fragment shader.
            void main()
            {
                // Will be used for attenuation.
                float distance = length(u_LightPos - v_Position);
             
                // Get a lighting direction vector from the light to the vertex.
                vec3 lightVector = normalize(u_LightPos - v_Position);
             
                // Calculate the dot product of the light vector and vertex normal. If the normal and light vector are
                // pointing in the same direction then it will get max illumination.
                float diffuse = max(dot(v_Normal, lightVector), 0.1);
             
                // Add attenuation.
                diffuse = diffuse * (1.0 / (1.0 + (0.1 * distance * distance)));
             
                // Multiply the color by the diffuse illumination level to get final output color.
                gl_FragColor = v_Color * diffuse; // 여러색
                //gl_FragColor = u_Color * diffuse;   // 단색
            }
        """.trimIndent()

    val color = floatArrayOf(0.5f, 0.5f, 0.5f, 1.0f)   // 색상 지정

    private var vertexBuffer: FloatBuffer =
        // bytebuffer 직접 할당하기
        ByteBuffer.allocateDirect(Cube2.cubeCoordinates.size * 4).run {
            // 기기는 nativer byte 순서를 사용한다.
            order(ByteOrder.nativeOrder())
            // bytebuffer로 부터 floatBuffer을 만들어낸다.
            asFloatBuffer().apply {
                // floatbuffer을 위한 좌표를 추가한다.
                put(Cube2.cubeCoordinates)
                // 첫번째 좌표를 읽기 버퍼를 설정한다.
                position(0)
            }
        }
    private var normalBuffer: FloatBuffer =
        ByteBuffer.allocateDirect(Cube2.cubeNormalData.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(Cube2.cubeNormalData)
                position(0)
            }
        }
    private var colorBuffer: FloatBuffer =
        ByteBuffer.allocateDirect(Cube2.cubeColorData.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(Cube2.cubeColorData)
                position(0)
            }
        }
    private var mProgram: Int
    init {
        val vertexShader: Int = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader: Int = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        // 텅 비어있는 opengl es program 만들기
        mProgram = GLES20.glCreateProgram().also {
            // 프로그램에 vertex shader 추가하기
            GLES20.glAttachShader(it, vertexShader)
            // 프로그램에 fragment shader 추가하기
            GLES20.glAttachShader(it, fragmentShader)

//            GLES20.glBindAttribLocation(it, 0, "a_Position")
//            GLES20.glBindAttribLocation(it, 1,  "a_Color")
//            GLES20.glBindAttribLocation(it, 2, "a_Normal")


            // opengl es 프로그램 실행할 수 있게 만들기
            GLES20.glLinkProgram(it)
        }
    }

    // 그리기 위한 프로그램 핸들 변수 설정
    private var mMVPMatrixHandle = 0
    private var mMVMMatrixHandle = 0
    private var mLightPosHandle = 0
    private var mPositionHandle = 0
    private var mColorHandle = 0
    private var mNormalHandle = 0

    private val vertexCount: Int = Cube2.cubeCoordinates.size / Cube2.COORDINATE_PER_VERTEX
    private val vertexStride: Int = Cube2.COORDINATE_PER_VERTEX * 4   // 한 꼭지점(vertex)당 4byte

    fun draw(mvpMatrix: FloatArray, mvmMatrix: FloatArray, mLightPosInEyeSpace: FloatArray){
        // opengl ES 환경에 프로그램 추가하기
        GLES20.glUseProgram(mProgram)

        //
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "a_Position").also {
            // 정점 추가. enable
            GLES20.glEnableVertexAttribArray(it)

            // 큐브 좌표 데이터 준비하기 (만들기 위한것) attribute
            vertexBuffer.position(0)
            GLES20.glVertexAttribPointer(
                it,
                Cube2.COORDINATE_PER_VERTEX,
                GLES20.GL_FLOAT,
                false,
                vertexStride,
                vertexBuffer
            )
            // color uniform or glGetAttribLocation
            colorBuffer.position(0)
            mColorHandle = GLES20.glGetAttribLocation(mProgram, "a_Color").also { colorHandle ->
                // 삼각형 그릴때 색 설정
                //GLES20.glUniform4fv(colorHandle, 1, color, 0)
                GLES20.glEnableVertexAttribArray(colorHandle)
                GLES20.glVertexAttribPointer(
                    colorHandle,
                    Cube2.COLOR_DATA_SIZE,
                    GLES20.GL_FLOAT,
                    false,
                    COLOR_DATA_SIZE*4,
                    colorBuffer
                )
//                GLES20.glDisableVertexAttribArray(colorHandle)
            }
            mColorHandle = GLES20.glGetUniformLocation(mProgram, "u_Color").also { colorHandle ->
                GLES20.glUniform4fv(colorHandle, 1, color, 0)
            }

            // normal attribute
            normalBuffer.position(0)
            mNormalHandle = GLES20.glGetAttribLocation(mProgram, "a_Normal").also { normalHandle ->
                GLES20.glEnableVertexAttribArray(normalHandle)
                GLES20.glVertexAttribPointer(
                    normalHandle,
                    Cube2.NORMAL_DATA_SIZE,
                    GLES20.GL_FLOAT,
                    false,
                    NORMAL_DATA_SIZE *4,
                    normalBuffer
                )
//                GLES20.glDisableVertexAttribArray(normalHandle)
            }

            // MVM
            mMVMMatrixHandle = GLES20.glGetUniformLocation(mProgram, "u_MVMatrix").also { mvmMatrixHandle ->
                GLES20.glUniformMatrix4fv(mvmMatrixHandle, 1, false, mvmMatrix, 0)
            }

            // MVP
            mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "u_MVPMatrix").also { mvpMatrixHandle ->
                GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)
            }

            // Light
            mLightPosHandle = GLES20.glGetUniformLocation(mProgram, "u_LightPos").also { lightPosHandle ->
                GLES20.glUniform3f(lightPosHandle, mLightPosInEyeSpace[0], mLightPosInEyeSpace[1], mLightPosInEyeSpace[2])
            }


            // 삼각형 그리기
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount)

            // disable vertex array // diable 하기
            GLES20.glDisableVertexAttribArray(mColorHandle)
            GLES20.glDisableVertexAttribArray(mNormalHandle)
            GLES20.glDisableVertexAttribArray(it)

        }
    }
}

class Lighting {
    private val vertexShaderCode = """
        uniform mat4 uMVPMatrix;
        attribute vec4 vPosition;
        void main() {
            gl_Position = uMVPMatrix * vPosition;
            gl_PointSize = 10.0;
        }
    """.trimIndent()
    private val fragmentShaderCode = """
        precision mediump float;
        void main() {
            gl_FragColor = vec4(1.0, 1.0, 1.0, 1.0);
        }
    """.trimIndent()
    private val mProgram: Int
    init {
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        mProgram = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vertexShader)
            GLES20.glAttachShader(it, fragmentShader)
            GLES20.glLinkProgram(it)
        }
    }

    private var positionHandle = 0
    private var mvpMatrixHandle = 0
    fun draw(mvpMatrix: FloatArray, mLightPosInModelSpace: FloatArray) {
        GLES20.glUseProgram(mProgram)

        positionHandle = GLES20.glGetAttribLocation(mProgram,"vPosition").also {
            GLES20.glEnableVertexAttribArray(it)

            //
            GLES20.glVertexAttrib3f(it,mLightPosInModelSpace[0], mLightPosInModelSpace[1], mLightPosInModelSpace[2])

            GLES20.glDisableVertexAttribArray(it)

            mvpMatrixHandle = GLES20.glGetUniformLocation(mProgram,"uMVPMatrix")
            GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix,0)
            
            // 붉빛 그리기
            GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 1)

        }

    }
}

@Composable
fun Greeting(name: String, glSurfaceView: GLSurfaceView? = null) {
    Text(text = "Hello $name!")
    if (glSurfaceView != null)
        AndroidView(factory = {
            glSurfaceView
        })
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    OpenglTestTheme {
        Greeting("Android")
    }
}