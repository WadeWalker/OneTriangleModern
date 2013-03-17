package name.wadewalker.onetrianglemodern;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Random;

import javax.media.opengl.GL;
import javax.media.opengl.GL2ES2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;

import junit.framework.Assert;

//==============================================================================
/**
 * Draws one textured triangle in a modern style, using only vertex and
 * fragment shaders instead of the fixed-function pipeline.
 *
 * @author Wade Walker
 */
public class OneTriangle implements GLEventListener {

    /** Width and height of texture. */
    private static final int siTexSize = 128;

    /** Program object index. */
    private int iProgram;
    
    /** Vertex shader index. */
    private int iVertexShader;

    /** Fragment shader index. */
    private int iFragmentShader;

    /** Texture index. */
    private int iTextureIndex;

    /** Index of the vertex attribute (input to the vertex shader). */
    private int iVertexAttributeLocation;

    /** Index of the texture coordinate attribute (input to the vertex shader). */
    private int iTexCoordAttributeLocation;

    /** Index of texture sampler (input to the fragment shader). */
    private int iTextureSamplerLocation;

    //==============================================================================
    /**
     * Constructor.
     */
    public OneTriangle() {
    }

    //==============================================================================
    /**
     * Creates the shaders, vertex buffer, and texture.
     * @param glautodrawable Used to get the GL object.
     * @see javax.media.opengl.GLEventListener#init(javax.media.opengl.GLAutoDrawable)
     */
    public void init( GLAutoDrawable glautodrawable ) {
        GL2ES2 gl = glautodrawable.getGL().getGL2ES2();

        int [] aiMaxTextureSize = new int[1];
        gl.glGetIntegerv( GL2ES2.GL_MAX_TEXTURE_SIZE, aiMaxTextureSize, 0 );
        Assert.assertTrue( "Texture size " + siTexSize + " bigger than max allowable size of " + aiMaxTextureSize[0] + ".", siTexSize <= aiMaxTextureSize[0] );

        iProgram = gl.glCreateProgram();
        Assert.assertTrue( "glCreateProgram failed.", iProgram != 0 );

        iVertexShader = ShaderUtils.createAndAttachShader( gl, iProgram, "src/name/wadewalker/onetrianglemodern/OneTriangle.vs", GL2ES2.GL_VERTEX_SHADER );
        iFragmentShader = ShaderUtils.createAndAttachShader( gl, iProgram, "src/name/wadewalker/onetrianglemodern/OneTriangle.fs", GL2ES2.GL_FRAGMENT_SHADER );

        gl.glLinkProgram( iProgram );
        ShaderUtils.checkProgramValid( gl, iProgram );

        gl.glUseProgram( iProgram );

        int [] aiVertexBuffer = new int [1];
        int [] aiLocation = new int [2];
        ShaderUtils.createAndFillBuffer( gl, iProgram,
            new String [] {"vVertex", "vTexCoord"}, new int [] {3, 2}, new int [] {0, 3},
            aiVertexBuffer, aiLocation,
            new float [] {-1.0f, -1.0f,  0.0f,    // vVertex 0
                           0.0f,  0.0f,           // vTexCoord 0
                           0.0f,  1.0f,  0.0f,    // vVertex 1
                           0.5f,  1.0f,           // vTexCoord 1
                           1.0f, -1.0f,  0.0f,    // vVertex 2
                           1.0f,  0.0f} );        // vTexCoord 2

        iVertexAttributeLocation = aiLocation[0];
        iTexCoordAttributeLocation = aiLocation[1];

        // create texture A
        FloatBuffer floatbufferA = createRandomFloatTexture( siTexSize, siTexSize );
//        FloatBuffer floatbufferA = createConstFloatTexture( siTexSize, siTexSize, 0.5f );
//        ByteBuffer bytebufferA = createRandomByteTexture( siTexSize, siTexSize );

        iTextureIndex = ShaderUtils.createAndFillTexture( gl, iProgram, siTexSize, siTexSize, floatbufferA );
        iTextureSamplerLocation = gl.glGetUniformLocation ( iProgram, "texture" );
        Assert.assertTrue( "glGetUniformLocation failed.", iTextureSamplerLocation != -1 );
        ShaderUtils.checkGLError( gl, "glGetUniformLocation" );

        gl.glClearColor( 0, 0, 0, 1 );
        gl.glEnable( GL2ES2.GL_DEPTH_TEST );
        gl.glUseProgram( 0 );
    }

    //==============================================================================
    /**
     * Creates an RGBA buffer filled with random floats of length width x height.
     * @param iWidth Width of buffer.
     * @param iHeight Height of buffer.
     * @return the new buffer.
     */
    private FloatBuffer createRandomFloatTexture( int iWidth, int iHeight ) {

        Random random = new Random();
        ByteBuffer bytebuffer = ByteBuffer.allocateDirect( iWidth * iHeight * 4 * ShaderUtils.siGLFloatBytes );
        bytebuffer.order( ByteOrder.nativeOrder() );
        FloatBuffer floatbuffer = bytebuffer.asFloatBuffer();

        for( int iH = 0; iH < iHeight; iH++ ) {
            for( int iW = 0; iW < iWidth; iW++ ) {
                floatbuffer.put( random.nextFloat() );
                floatbuffer.put( random.nextFloat() );
                floatbuffer.put( random.nextFloat() );
                floatbuffer.put( random.nextFloat() );
            }
        }
        floatbuffer.flip();
        return( floatbuffer );
    }

    //==============================================================================
    /**
     * Creates an RGBA buffer filled with a constant float value of length width x height.
     * @param iWidth Width of buffer.
     * @param iHeight Height of buffer.
     * @param fConst Constant value to assign to every element of the buffer.
     * @return the new buffer.
     */
    @SuppressWarnings("unused")
    private FloatBuffer createConstFloatTexture( int iWidth, int iHeight, float fConst ) {

        ByteBuffer bytebuffer = ByteBuffer.allocateDirect( iWidth * iHeight * 4 * ShaderUtils.siGLFloatBytes );
        bytebuffer.order( ByteOrder.nativeOrder() );
        FloatBuffer floatbuffer = bytebuffer.asFloatBuffer();

        for( int iH = 0; iH < iHeight; iH++ ) {
            for( int iW = 0; iW < iWidth; iW++ ) {
                floatbuffer.put( fConst );
                floatbuffer.put( fConst );
                floatbuffer.put( fConst );
                floatbuffer.put( fConst );
            }
        }
        floatbuffer.flip();
        return( floatbuffer );
    }

    //==============================================================================
    /**
     * Creates an RGBA buffer of length width x height with constant float values on
     * the diagonal, and zeros everywhere else.
     * @param iWidth Width of buffer.
     * @param iHeight Height of buffer.
     * @param fConst Constant value to assign to every element of the buffer.
     * @return the new buffer.
     */
    @SuppressWarnings("unused")
    private FloatBuffer createDiagonalFloatTexture( int iWidth, int iHeight, float fConst ) {

        ByteBuffer bytebuffer = ByteBuffer.allocateDirect( iWidth * iHeight * 4 * ShaderUtils.siGLFloatBytes );
        bytebuffer.order( ByteOrder.nativeOrder() );
        FloatBuffer floatbuffer = bytebuffer.asFloatBuffer();

        for( int iH = 0; iH < iHeight; iH++ ) {
            for( int iW = 0; iW < iWidth; iW++ ) {
                if( iH == iW ) {
                    floatbuffer.put( fConst );
                    floatbuffer.put( fConst );
                    floatbuffer.put( fConst );
                    floatbuffer.put( fConst );
                }
                else {
                    floatbuffer.put( 0.0f );
                    floatbuffer.put( 0.0f );
                    floatbuffer.put( 0.0f );
                    floatbuffer.put( 0.0f );
                }
            }
        }
        floatbuffer.flip();
        return( floatbuffer );
    }

    //==============================================================================
    /**
     * Creates a buffer filled with random bytes of length width x height.
     * @param iWidth Width of buffer.
     * @param iHeight Height of buffer.
     * @return the new buffer.
     */
    @SuppressWarnings("unused")
    private ByteBuffer createRandomByteTexture( int iWidth, int iHeight ) {

        Random random = new Random();
        ByteBuffer bytebuffer = ByteBuffer.allocateDirect( iWidth * iHeight * 4 );
        bytebuffer.order( ByteOrder.nativeOrder() );

        byte [] ab = new byte [4];
        for( int iH = 0; iH < iHeight; iH++ ) {
            for( int iW = 0; iW < iWidth; iW++ ) {
                random.nextBytes( ab );
                bytebuffer.put( ab );
            }
        }
        bytebuffer.flip();
        return( bytebuffer );
    }

    //==============================================================================
    /**
     * Draws the textured square.
     * @param glautodrawable Used to get the GL object.
     * @see javax.media.opengl.GLEventListener#display(javax.media.opengl.GLAutoDrawable)
     */
    public void display( GLAutoDrawable glautodrawable ) {

        GL2ES2 gl = glautodrawable.getGL().getGL2ES2();

        gl.glClear( GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT );

        gl.glUseProgram( iProgram );
        gl.glEnableVertexAttribArray( iVertexAttributeLocation );
        gl.glEnableVertexAttribArray( iTexCoordAttributeLocation );

        gl.glActiveTexture( GL2ES2.GL_TEXTURE0 );
        gl.glBindTexture( GL2ES2.GL_TEXTURE_2D, iTextureIndex );
        gl.glUniform1i( iTextureSamplerLocation, 0 );

        gl.glDrawArrays( GL.GL_TRIANGLE_STRIP, 0, 4 );

        gl.glDisableVertexAttribArray( iVertexAttributeLocation );
        gl.glDisableVertexAttribArray( iTexCoordAttributeLocation );
        gl.glUseProgram( 0 );
        gl.glBindFramebuffer( GL2ES2.GL_FRAMEBUFFER, 0 );
    }

    //==============================================================================
    /**
     * Not used, since we're drawing in window coordinates.
     * @see javax.media.opengl.GLEventListener#reshape(javax.media.opengl.GLAutoDrawable, int, int, int, int)
     */
    public void reshape( GLAutoDrawable glautodrawable, int x, int y, int width, int height ) {
    }

    //==============================================================================
    /**
     * Cleans up GL objects.
     * @param glautodrawable Used to get the GL object.
     * @see javax.media.opengl.GLEventListener#dispose(javax.media.opengl.GLAutoDrawable)
     */
    public void dispose( GLAutoDrawable glautodrawable ) {

        GL2ES2 gl = glautodrawable.getGL().getGL2ES2();
        gl.glDeleteTextures( 1, new int [] {iTextureIndex}, 0 );
        gl.glDetachShader( iProgram, iVertexShader );
        gl.glDeleteShader( iVertexShader );
        gl.glDetachShader( iProgram, iFragmentShader );
        gl.glDeleteShader( iFragmentShader );
        gl.glDeleteProgram( iProgram );
    }
}
