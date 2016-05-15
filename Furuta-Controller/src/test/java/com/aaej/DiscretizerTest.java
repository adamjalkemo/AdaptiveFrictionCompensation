package com.aaej;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import Jama.*;

/**
 * Unit test for the Discretizer class.
 * Can be used to verify error magnitude of the Riccati solver when using different number of iterations.
 */
public class DiscretizerTest
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public DiscretizerTest(String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( DiscretizerTest.class );
    }

    /**
     * Test of a small matrix (not used in any controller)
     */
    public void testSmallMatrix() {
        Matrix A = new Matrix(new double[][]{{-2, 0},{0, -1}});
        
        Matrix B = new Matrix(new double[]{1, 0.1},2);

        Matrix Q = new Matrix(new double[][]{{1,0},{0,1}});
        Matrix R = new Matrix(new double[]{1},1);

        assertEquals( (double) -1.7291, (double) Discretizer.getL(A,B,Q,R).getArray()[0][0], (double) 0.0001);
        assertEquals( (double) 0.4214, (double) Discretizer.getL(A,B,Q,R).getArray()[0][1], (double) 0.0001);
    }

    /**
     * Test of solver using the real Matrices used.
     */
    public void testActualMatrix() {
            Matrix A = new Matrix(new double[][]{{1.00156624468639, 0.0100052202706862, 0, 0},{0.313330682422588, 1.00156624468639, 0, 0},{-0.0000294273019572104, -0.0000000980807680405294, 1, 0.01},{-0.00588699625557634, -0.0000294273019572104, 0, 1}});
            Matrix B = new Matrix(new double[]{-0.00356262735559447, -0.712711411086724, 0.00956230711339704, 1.91246491620224},4);

            Matrix Q = new Matrix(new double[][]{{100, 0, 0, 0},{0, 1, 0, 0},{0, 0, 10, 0},{0, 0, 0, 10}});
            Matrix R = new Matrix(new double[]{100},1);

            assertEquals( (double) -8.834914663160147, (double) Discretizer.getL(A,B,Q,R).getArray()[0][0], (double) 0.0001);
            assertEquals( (double) -1.580364776252613, (double) Discretizer.getL(A,B,Q,R).getArray()[0][1], (double) 0.0001);
            assertEquals( (double) -0.220472707928114, (double) Discretizer.getL(A,B,Q,R).getArray()[0][2], (double) 0.0001);
            assertEquals( (double) -0.304872980032455, (double) Discretizer.getL(A,B,Q,R).getArray()[0][3], (double) 0.0001);
    }
}
