/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2002  Gerald Brose.
 *
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Library General Public
 *   License as published by the Free Software Foundation; either
 *   version 2 of the License, or (at your option) any later version.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this library; if not, write to the Free
 *   Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package org.jacorb.idl;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.util.GlobPatternMapper;
import org.apache.tools.ant.util.SourceFileScanner;

import java.util.Vector;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This is the idl compile task for using the idl compiler
 * from the ANT build tool.
 *
 * @author Wei-ju Wu
 * @version 1.0
 */
public class JacIDL extends MatchingTask
{

    private File _destdir;
    private File _srcdir;
    private Path _includepath;
    private String _packageprefix;

    private int _debuglevel;
    private boolean _generateir;
    private boolean _generateincluded;
    private boolean _parseonly;
    private boolean _globalimport;
    private boolean _noskel;
    private boolean _nostub;
    private boolean _sloppyforward;
    private boolean _sloppynames;
    private boolean _includestate;

    private List _defines = new ArrayList();
    private List _undefines = new ArrayList();
    private File _compileList[] = new File[ 0 ];

    public JacIDL()
    {
        parser.command_line = null;
        parser.import_list = new java.util.Vector();
        parser.pack_replace = new java.util.Hashtable();
        parser.scopes = new java.util.Stack();
        parser.include_state = false;
        _destdir = new File( "." );
        _srcdir = new File( "." );
        _packageprefix = null;
        _parseonly = false;
        _globalimport = false;
        _generateir = false;
        _noskel = false;
        _nostub = false;
        _generateincluded = false;
        _debuglevel = 0;
    }

    /**
     * Set the destination directory.
     * @param dir the destination directory
     */
    public void setDestdir( File dir )
    {

        _destdir = dir;
    }

    /**
     * Set the source directory.
     * @param dir the source directory
     */
    public void setSrcdir( File dir )
    {

        _srcdir = dir;
    }

    /**
     * Set the include path for the idl compiler.
     * @param path the include path
     */
    public void setIncludepath( Path path )
    {

        _includepath = path;
    }

    /**
     * Set the prefix to prepend to the package names.
     * @param prefix the prefix
     */
    public void setPackageprefix( String prefix )
    {

        _packageprefix = prefix;
    }

    /**
     * Set the debug level.
     * @param level the debug level
     */
    public void setDebuglevel( int level )
    {

        _debuglevel = level;
    }

    // ****************************************************************
    // **** Set the flags
    // ******************************

    /**
     * Set the flag to generate the interface repository files.
     * @param flag the flag
     */
    public void setGenerateir( boolean flag )
    {

        _generateir = flag;
    }

    /**
     * Set the flag to generate all files.
     * @param flag the flag
     */
    public void setAll( boolean flag )
    {

        _generateincluded = flag;
    }

    /**
     * Set the flag to parse the idl only.
     * @param flag the flag
     */
    public void setParseonly( boolean flag )
    {

        _parseonly = flag;
    }

    /**
     * Set the flag to use global imports.
     * @param flag the flag
     */
    public void setGlobalimport( boolean flag )
    {

        _globalimport = flag;
    }

    /**
     * Set the flag to leave out skeleton generation.
     * @param flag the flag
     */
    public void setNoskel( boolean flag )
    {

        _noskel = flag;
    }

    /**
     * Set the flag to leave out stub generation.
     * @param flag the flag
     */
    public void setNostub( boolean flag )
    {

        _nostub = flag;
    }

    /**
     * Set the flag to use sloppy forwards.
     * @param flag the flag
     */
    public void setSloppyforward( boolean flag )
    {

        _sloppyforward = flag;
    }

    /**
     * Set the flag to use sloppy names.
     * @param flag the flag
     */
    public void setSloppynames( boolean flag )
    {

        _sloppynames = flag;
    }

    // ****************************************************************
    // **** Nested elements
    // ******************************

    public void addDefine( org.apache.tools.ant.types.Environment.Variable
            def )
    {
        // The variable can only be evaluated in the execute() method
        _defines.add( def );
    }

    public void addUndefine( org.apache.tools.ant.types.Environment.Variable
            def )
    {
        // The variable can only be evaluated in the execute() method
        _undefines.add( def );
    }

    // *****************************************************************

    /**
     * The execute() method of the task.
     * @throws BuildException
     */
    public void execute() throws BuildException
    {
        parser myparser = null;

        parser.init ();

        // set destination directory
        if ( ! _destdir.exists ())
        {
            _destdir.mkdirs ();
        }
        parser.out_dir = _destdir.getPath();

        // Generate code for all IDL files, even included ones
        parser.generateIncluded = _generateincluded;

        // generate interface repository
        parser.generateIR = _generateir;

        // global import
        parser.global_import = _globalimport;

        // parse only
        parser.parse_only = _parseonly;

        // no skeletons
        parser.generate_skeletons = ( !_noskel );

        // no stubs
        parser.generate_stubs = ( !_nostub );

        // sloppy forwards
        parser.sloppy = _sloppyforward;

        // sloppy names
        parser.strict_names = ( !_sloppynames );

        // include path
        if( _includepath != null )
        {

            // Check path
            String includeList[] = _includepath.list();
            for( int i = 0; i < includeList.length; i++ )
            {

                File incDir = project.resolveFile( includeList[ i ] );
                if( !incDir.exists() )
                {

                    throw new BuildException( "include directory \"" +
                            incDir.getPath() + "\" does not exist !", location );
                }
            }
            GlobalInputStream.setIncludePath( _includepath.toString() );
        }

        // package prefix
        if( _packageprefix != null && _packageprefix.length() > 0 )
        {
            parser.package_prefix = _packageprefix;
        }

        // setup input file lists
        resetFileLists();
        DirectoryScanner ds = getDirectoryScanner( _srcdir );
        String files[] = ds.getIncludedFiles();
        //log("files: "+files);
        scanFiles( files );

        // ***********************************
        // **** invoke parser
        // ***********************************
        // invoke the parser for parsing the files that were
        // specified in the task specification
        try
        {
            if( _compileList != null )
            {

                for( int i = 0; i < _compileList.length; i++ )
                {

                    // setup the parser
                    String fileName = _compileList[ i ].getPath();
                    log( "processing idl file: " + fileName );
                    Environment.output( 4, fileName );
                    GlobalInputStream.init();
                    GlobalInputStream.setInput( fileName );
                    lexer.reset();
                    NameTable.init();
                    ConstDecl.init();
                    TypeMap.init();
                    setupDefines();

                    myparser = new parser();
                    myparser.parse();
                }
            }

        }
        catch( IOException ioex )
        {

            ioex.printStackTrace();

        }
        catch( Exception ex )
        {

            ex.printStackTrace();
        }
    }

    /**
     * Clear the list of files to be compiled and copied..
     */
    protected void resetFileLists()
    {
        _compileList = new File[ 0 ];
    }

    /**
     * Scans the directory looking for source files to be compiled.
     * The results are returned in the class variable compileList
     */
    protected void scanFiles( String files[] ) throws BuildException
    {
        File file;

        // TODO: create an own pattern mapper
        GlobPatternMapper m = new GlobPatternMapper();
        m.setFrom( "*.idl" );
        m.setTo( "*.java" );
        SourceFileScanner sfs = new SourceFileScanner( this );
        File[] newfiles = sfs.restrictAsFiles( files, _srcdir, _destdir, m );
        _compileList = new File[ newfiles.length ];

        for( int i = 0; i < newfiles.length; i++ )
        {
            log( "scan file: " + newfiles[ i ].getPath() );
            file = newfiles[ i ];
            if( !file.exists() )
            {

                throw new BuildException( "The input file \"" + file.getPath() +
                        "\" does not exist !" );
            }
            _compileList[ i ] = file;
        }
    }

    public File[] getFileList()
    {

        return _compileList;
    }

    private static boolean fileExists( String filename )
    {

        if( filename == null || filename.length() == 0 ) return false;
        File file = new File( filename );
        return ( file.exists() && file.isFile() );
    }

    private static boolean dirExists( File dir )
    {

        return ( dir.exists() && dir.isDirectory() );
    }

    private void setupDefines()
    {

        org.apache.tools.ant.types.Environment.Variable prop;
        String value;

        for( int i = 0; i < _defines.size(); i++ )
        {

            prop = (org.apache.tools.ant.types.Environment.Variable)
                    _defines.get( i );
            value = prop.getValue();
            if( value == null ) value = "1";
            lexer.define( prop.getKey(), value );
        }
        for( int i = 0; i < _undefines.size(); i++ )
        {

            prop = (org.apache.tools.ant.types.Environment.Variable)
                    _undefines.get( i );
            lexer.undefine( prop.getKey() );
        }
    }
}
