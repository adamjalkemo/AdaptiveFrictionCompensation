package com.aaej;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import Jama.*;
import java.util.*;

/**
 * Unit test for simple App.
 */
public class DlqrTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public DlqrTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( DlqrTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        Matrix A = new Matrix(new double[][]{{-2, 0},{0, -1}});
        
        Matrix B = new Matrix(new double[]{1, 0.1},2);

        Matrix Q = new Matrix(new double[][]{{1,0},{0,1}});
        Matrix R = new Matrix(new double[]{1},1);

        //System.out.println(Arrays.deepToString(Dlqr.getL(A,B,Q,R).getArray()));


        //  assertEquals( "Messaefjaepifjaepifjiaejfijaiepfj", new double[]{-1.7291,0.4214} , Dlqr.getL(A,B,Q,R).getArray()[0], 0.0);
        assertEquals( (double) -1.7291, (double) Dlqr.getL(A,B,Q,R).getArray()[0][0], (double) 0.0001);
        assertEquals( (double) 0.4214, (double) Dlqr.getL(A,B,Q,R).getArray()[0][1], (double) 0.0001);
    }
}
