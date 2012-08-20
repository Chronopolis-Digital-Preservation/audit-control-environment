/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umiacs.ace.ims.processor;

/**
 *
 * @author mmcgann
 */
public interface ResponseDestination 
{
    void enqueue(WorkUnit response);    
}
