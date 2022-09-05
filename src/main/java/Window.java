import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {

    private int width, height;
    private String title;
    private int targetMonitor;
    private boolean fullscreen;
    private long windowHandle;
    private int frames;
    private long time;

    private Input input;

    public Window(int width, int height, String title, int targetMonitor, boolean fullscreen) {
        this.width = width;
        this.height = height;
        this.title = title;
        this.targetMonitor = targetMonitor;
        this.fullscreen = fullscreen;
        this.input = new Input();
    }

    public void create() {
        // Set up an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if ( !glfwInit() )
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

        // Create the window
        windowHandle = glfwCreateWindow(width, height, title, NULL, NULL);
        if ( windowHandle == NULL )
            throw new RuntimeException("Failed to create the GLFW window");

        // Get the target monitor
        PointerBuffer monitors = glfwGetMonitors();
        long monitor = monitors.get(targetMonitor);

        // Get the resolution and position of the target monitor
        GLFWVidMode videoMode = glfwGetVideoMode(monitor);

        IntBuffer monitorX = BufferUtils.createIntBuffer(1);
        IntBuffer monitorY = BufferUtils.createIntBuffer(1);
        glfwGetMonitorPos(monitor, monitorX, monitorY);

        // Center the window
        glfwSetWindowPos(
                windowHandle,
                monitorX.get(0) + (videoMode.width() - width) / 2,
                monitorY.get(0) + (videoMode.height() - height) / 2
        );

        glfwMakeContextCurrent(windowHandle);
        GL.createCapabilities();

        createCallbacks();

        // Make the OpenGL context current
        glfwMakeContextCurrent(windowHandle);
        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(windowHandle);

        // Hides the cursor and locks it to the window
        org.lwjgl.glfw.GLFW.glfwSetInputMode(windowHandle, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
        // Use raw mouse motion
        if (GLFW.glfwRawMouseMotionSupported())
            GLFW.glfwSetInputMode(windowHandle, GLFW.GLFW_RAW_MOUSE_MOTION, GLFW_TRUE);
    }

    public void update() {
        glfwPollEvents();

        // FPS tracker
        frames++;
        if (System.currentTimeMillis() > time + 1000) {
            GLFW.glfwSetWindowTitle(windowHandle, title + " | FPS: " + frames);
            time = System.currentTimeMillis();
            frames = 0;
        }
    }

    private void createCallbacks() {
        glfwSetFramebufferSizeCallback(windowHandle, (window, width, height) -> {
            this.width = width;
            this.height = height;
            glViewport(0, 0, width, height);
        });

        GLFW.glfwSetKeyCallback(windowHandle, input.getKeyboardCallback());
        GLFW.glfwSetCursorPosCallback(windowHandle, input.getMouseMoveCallback());
        GLFW.glfwSetMouseButtonCallback(windowHandle, input.getMouseButtonsCallback());
        GLFW.glfwSetScrollCallback(windowHandle, input.getMouseScrollCallback());
    }

    public void destroy() {
        glfwFreeCallbacks(windowHandle);
        glfwDestroyWindow(windowHandle);
        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    public void swapBuffers() {
        glfwSwapBuffers(windowHandle);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public long getWindowHandle() {
        return windowHandle;
    }

    public boolean shouldClose() {
        return glfwWindowShouldClose(windowHandle);
    }
}
