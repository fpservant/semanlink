package net.semanlink.util.index_B4_2020_04.jena;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Function;

import net.semanlink.util.index_B4_2020_04.IndexInterface;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Node_Literal;
import org.apache.jena.query.QueryBuildException;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.binding.BindingMap;
import org.apache.jena.sparql.engine.iterator.QueryIterNullIterator;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import org.apache.jena.sparql.pfunction.PropFuncArg;
import org.apache.jena.sparql.pfunction.PropFuncArgType;
import org.apache.jena.sparql.pfunction.PropertyFunctionEval;
import org.apache.jena.sparql.util.IterLib;
import org.apache.jena.util.iterator.Map1;
import org.apache.jena.util.iterator.Map1Iterator;
// import org.openjena.atlas.logging.Log;
// import org.apache.jena.atlas.logging.Log;
// ATTENTION: in TextMatchMagicProp2, it has been necessary to create literal with lang (necessary, or nasty bug). Should also be done here? -- CHECK IT

/**
 * @since 2010-12
 */
public class TextMatchMagicProp extends PropertyFunctionEval
{
		private static IndexInterface<Resource> index; // use getter. One by lang ??? TODO
		/** MUST be called */
		public static void setIndex(IndexInterface<Resource> textIndex) { index = textIndex; }
		public TextMatchMagicProp() // must be public or Class org.apache.jena.sparql.pfunction.PropertyFunctionFactoryAuto can not access a member of class sicg.euro5.magicprop.TextMatch with modifiers "protected"
    {
        // super(PropFuncArgType.PF_ARG_EITHER,
        //      PropFuncArgType.PF_ARG_EITHER) ;
        super(PropFuncArgType.PF_ARG_SINGLE,
            PropFuncArgType.PF_ARG_SINGLE) ;
    }
		
		// @TODO
		protected IndexInterface<Resource> getIndex(String lang) { return index; }

    @Override
    public void build(PropFuncArg argSubject, Node predicate, PropFuncArg argObject, ExecutionContext execCxt)
    {
        super.build(argSubject, predicate, argObject, execCxt) ;
        // if ( getIndex(execCxt) == null )
            // throw new QueryBuildException("Index not found") ;

        /*if ( argSubject.isList() && argSubject.getArgListSize() != 2 )
                throw new QueryBuildException("Subject has "+argSubject.getArgList().size()+" elements, not 2: "+argSubject) ;
        
        if ( argObject.isList() && (argObject.getArgListSize() != 2 && argObject.getArgListSize() != 3) )
                throw new QueryBuildException("Object has "+argObject.getArgList().size()+" elements, not 2 or 3: "+argObject) ;*/
        if ( argSubject.isList())
          throw new QueryBuildException("Subject is list -- that's unexpected") ;
  
        if ( argObject.isList())
          throw new QueryBuildException("Object is list -- that's unexpected") ;

    }
    
    @Override
    public QueryIterator execEvaluated(Binding binding, PropFuncArg argSubject, Node predicate, PropFuncArg argObject, ExecutionContext execCxt)
    {
    		return execEvaluatedProtected(binding, argSubject, predicate,  argObject,  execCxt) ;
    }

    private QueryIterator execEvaluatedProtected(Binding binding, PropFuncArg argSubject, Node predicate, PropFuncArg argObject, ExecutionContext execCxt)
    {	
        Node match = argSubject.getArg() ;
         
        Node searchStringNode = argObject.getArg() ;

        // 2017-06 commented out
        // if ( !isValidSearchString(searchStringNode) )
        //     return IterLib.noResults(execCxt) ;
        
        Node_Literal searchLit = (Node_Literal) searchStringNode;
        String lang = searchLit.getLiteralLanguage();
        String searchString = searchLit.getLiteralLexicalForm();
        
        // RDCLabelIndex index = euro5Service.rdcLabelIndex(lang);
        IndexInterface<Resource> index = getIndex(lang);
        if (index == null) {
       		// Log.warn(this, "No index for lang " + lang) ;
       		return IterLib.noResults(execCxt) ;
        }

        if ( match.isVariable() )
            return varSubject(binding, Var.alloc(match), searchString, index, execCxt) ;
        else
            return boundSubject(binding, match, searchString, index, execCxt) ;
    }
    
    private static boolean isValidSearchString(Node searchString)
    {
        if ( !searchString.isLiteral() )
        {
            // Log.warn(TextMatchMagicProp.class, "Not a string: "+searchString) ;
            return false ;
        }

        if ( searchString.getLiteralDatatypeURI() != null )
        {
            // Log.warn(TextMatchMagicProp.class, "Not a plain string: "+searchString) ;
            return false ;
        }

        /* if ( searchString.getLiteralLanguage() != null && ! searchString.getLiteralLanguage().equals("") )
        {
            Log.warn(textSearch.class, "Not a plain string (has lang tag): "+searchString) ;
            return false ;
        }*/
        return true ;
    }
    
    public QueryIterator varSubject(Binding binding, 
                                    Var match,
                                    String searchString,
                                    IndexInterface<Resource> index,
                                    ExecutionContext execCxt)
    {
    		// Iterator<HitLARQ> iter = getIndex(execCxt).search(searchString) ;
    		Collection<Resource> hits = index.searchText(searchString);

         
        HitConverter converter = new HitConverter(binding, match) ;
        
        // Iterator<Binding> iter2 = new Map1Iterator<HitLARQ, Binding>(converter, iter) ;
        Iterator<Binding> iter2 = new Map1Iterator<Resource, Binding>(converter, hits.iterator()) ;
        QueryIterator qIter = new QueryIterPlainWrapper(iter2, execCxt) ;

        return qIter ;
    }
    
    static class HitConverter implements Function<Resource, Binding>
    {
        private Binding binding ;
        private Var match ;
        
        HitConverter(Binding binding, Var matchVar)
        {
            this.binding = binding ;
            this.match = matchVar ;
         }
        
     // moving to jena 3 
        // public Binding map1(Resource hit)
		@Override
		public Binding apply(Resource hit) {
			// MOVING FROM JENA-ARQ 2.7.? TO 2.9.3 (under apache now), this doesn't compile anymore:
	          // Binding b = new BindingMap(binding) ;
	          BindingMap b = BindingFactory.create(binding) ;
	          b.add(match, hit.asNode()) ;
	          
	          return b;
		}
       
    }
    
    public QueryIterator boundSubject(Binding binding, 
                                      Node match,
                                      String searchString,
                                      IndexInterface<Resource> index,
                                      ExecutionContext execCxt)
    {
        // HitLARQ hit = getIndex(execCxt).contains(match, searchString) ;
    		Collection<Resource> hits = index.searchText(searchString);
     		boolean found = false;
     		String matchUri = match.getURI();
     		for(Resource res : hits) {
     			String resUri = res.getURI();
     			if (resUri.equals(matchUri)) {
     				found = true;
     				break;
     			}
     		}
     		
        if ( !found )
            return new QueryIterNullIterator(execCxt) ;
        // return IterLib.oneResult(binding, score, NodeFactory.floatToNode(hit.getScore()), execCxt) ;
        return IterLib.result(binding, execCxt) ; ////// ???????????????????
    }

}
