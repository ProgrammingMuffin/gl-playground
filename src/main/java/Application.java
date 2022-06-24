import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class Application {
    public static void main(String[] args) {
        GLFW.glfwInit();
        long window = GLFW.glfwCreateWindow(640, 480, "hello world!", GLFW.glfwGetPrimaryMonitor(), 0);
        GLFW.glfwDefaultWindowHints();
        GLFW.glfwMakeContextCurrent(window);
        GLFW.glfwShowWindow(window);
        GL.createCapabilities();
        float[] vertexBufferData = {
                -0.5f, -0.5f,
                -0.5f, 0.5f,
                0.5f, 0.5f,
                0.5f, -0.5f
        };
        GLUtil.setupDebugMessageCallback();
        int vao = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vao);
        int buffer = GL30.glGenBuffers();
        int p = GL30.glCreateProgram();
        int shader1, shader2;
        try {
            String vertexCode = readShaderCode("vertexshader.glsl");
            String pixelCode = readShaderCode("pixelshader.glsl");
            shader1 = compileShader(GL30.GL_VERTEX_SHADER, vertexCode);
            shader2 = compileShader(GL30.GL_FRAGMENT_SHADER, pixelCode);
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
        GL30.glAttachShader(p, shader1);
        GL30.glAttachShader(p, shader2);
        GL30.glLinkProgram(p);
        GL30.glUseProgram(p);
        GL30.glEnableVertexAttribArray(0);
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, buffer);
        GL30.glBufferData(GL30.GL_ARRAY_BUFFER, vertexBufferData, GL30.GL_STATIC_DRAW);
        //GL30.glTexImage2D(GL30.GL_TEXTURE_2D, 0, GL30.GL_RGB, 100, 100, 0, GL30.GL_RGB, GL30.GL_INT, textureBuffer);
        // 2 * (Integer.BYTES / Byte.SIZE),
        GL30.glBindAttribLocation(p, 0, "vertexCoords");
        GL30.glVertexAttribPointer(0, 2, GL30.GL_FLOAT, false, Float.BYTES * 2, 0);
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, 0);
        while (true) {
            GL30.glClear(GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT);
            GL30.glBindVertexArray(vao);
            GL30.glUseProgram(p);
            GL30.glDrawArrays(GL30.GL_QUADS, 0, 4);
            GLFW.glfwSwapBuffers(window);
            GL30.glUseProgram(0);
            GL30.glBindVertexArray(0);
            GLFW.glfwPollEvents();
        }
    }

    private static int compileShader(int type, String source) {
        int shader = GL30.glCreateShader(type);
        GL30.glShaderSource(shader, source);
        GL30.glCompileShader(shader);
        System.out.println("GL errors: " + GL30.glGetError());
        return shader;
    }

    private static ByteBuffer getTexture() {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D graphics = image.createGraphics();
        graphics.drawRect(0, 0, 100, 100);
        DataBuffer dataBuffer = image.getRaster().getDataBuffer();
        return ByteBuffer.wrap(((DataBufferByte) dataBuffer).getData());
    }

    private static String readShaderCode(String fileName) throws URISyntaxException, IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(Application.class.getClassLoader().getResource(fileName).toURI()))));
        String line;
        StringBuilder code = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            code.append(line);
            code.append("\n");
        }
        reader.close();
        return code.toString();
    }
}
