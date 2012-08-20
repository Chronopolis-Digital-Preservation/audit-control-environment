/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umiacs.ace.ims.tokenclass;

import java.util.List;
import javax.ejb.Local;

/**
 *
 * @author mmcgann
 */
@Local
public interface TokenClassLocal 
{
    TokenClass create(TokenClass tokenClass);
    List<TokenClass> list();
    TokenClass findByName(String name);
}
