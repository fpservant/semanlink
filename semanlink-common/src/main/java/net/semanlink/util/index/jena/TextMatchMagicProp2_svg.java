package net.semanlink.util.index.jena;

import java.util.*;

import net.semanlink.util.index.Index;
import net.semanlink.util.index.jena.ResourceLabelPair_SVG2;

import com.hp.hpl.jena.util.iterator.Map1;
import com.hp.hpl.jena.util.iterator.Map1Iterator;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Literal;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterNullIterator;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import com.hp.hpl.jena.sparql.pfunction.PropFuncArg;
import com.hp.hpl.jena.sparql.pfunction.PropFuncArgType;
import com.hp.hpl.jena.sparql.pfunction.PropertyFunctionEval;
import com.hp.hpl.jena.sparql.util.IterLib;
import com.hp.hpl.jena.query.QueryBuildException;
import com.hp.hpl.jena.query.QueryExecException;
// import org.openjena.atlas.logging.Log;
// import org.apache.jena.atlas.logging.Log;


/**
 * Property function to search for resources indexed by text.
 * <p>Example of use: to get at the same time the found resource and the matched text</p><pre>
 * SELECT ?res ?textMatch WHERE {
 * 		(?res ?textMatch) <http://www.renault.com/euro5/schema#magic_symptomLabel> "fum blanche"@fr.
 * }
 * </pre>
 * <p>Based on com.hp.hpl.jena.query.larq.LuceneSearch</p>
 */

public class TextMatchMagicProp2_svg extends PropertyFunctionEval
{
		private static Index<ResourceLabelPair_SVG2> index;
		/** MUST be called */
		public static void setIndex(Index<ResourceLabelPair_SVG2> textIndex) { index = textIndex; }
		
		public TextMatchMagicProp2_svg() // must be public or Class com.hp.hpl.jena.sparql.pfunction.PropertyFunctionFactoryAuto can not access a member of class package net.semanlink.util.jena.TextMatchMagicProp2 with modifiers "protected"
    {
        // super(PropFuncArgType.PF_ARG_EITHER,
        //      PropFuncArgType.PF_ARG_EITHER) ;
			
				// this is OK if not using list as subject
        // super(PropFuncArgType.PF_ARG_SINGLE, PropFuncArgType.PF_ARG_SINGLE) ;
			 super(PropFuncArgType.PF_ARG_EITHER, PropFuncArgType.PF_ARG_SINGLE) ;
    }

    @Override
    public void build(PropFuncArg argSubject, Node predicate, PropFuncArg argObject, ExecutionContext execCxt)
    {
        super.build(argSubject, predicate, argObject, execCxt) ;
        // if ( getIndex(execCxt) == null )
            // throw new QueryBuildException("Index not found") ;

        if ( argSubject.isList() && argSubject.getArgListSize() != 2 )
                throw new QueryBuildException("Subject has "+argSubject.getArgList().size()+" elements, not 2: "+argSubject) ;
        
        /*if ( argObject.isList() && (argObject.getArgListSize() != 2 && argObject.getArgListSize() != 3) )
                throw new QueryBuildException("Object has "+argObject.getArgList().size()+" elements, not 2 or 3: "+argObject) ;*/
        /* if ( argSubject.isList())
          throw new QueryBuildException("Subject is list -- that's unexpected") ; */
  
        if (argObject.isList())
          throw new QueryBuildException("Object is list -- that's unexpected") ;

    }
    
    @Override
    public QueryIterator execEvaluated(Binding binding, PropFuncArg argSubject, Node predicate, PropFuncArg argObject, ExecutionContext execCxt)
    {
    	try { 
    		return execEvaluatedProtected(binding, argSubject, predicate,  argObject,  execCxt) ;
    	} catch (RuntimeException ex)
    	{
    		// Log.fatal(this, "Exception from text search", ex) ;
    		throw ex ;
    	}
    }

    private QueryIterator execEvaluatedProtected(Binding binding, PropFuncArg argSubject, Node predicate, PropFuncArg argObject, ExecutionContext execCxt)
    {	
    	Node match = null, textMatch = null;

      if ( argSubject.isList() )
      {
          // Length checked in build
          match = argSubject.getArg(0) ;
          textMatch = argSubject.getArg(1) ;
          
          if ( ! textMatch.isVariable() )
              throw new QueryExecException("Hit textMatch is not a variable: "+argSubject) ;
      }
      else
      {
        match = argSubject.getArg() ;
      }
         
      Node searchStringNode = argObject.getArg() ;

      if ( !isValidSearchString(searchStringNode) )
          return IterLib.noResults(execCxt) ;
      
      Node_Literal searchLit = (Node_Literal) searchStringNode;
      String lang = searchLit.getLiteralLanguage();
      String searchString = searchLit.getLiteralLexicalForm();
      
      /*
    	// @find same property function for 2 different prop URIs
      ModelIndexedByLabel2 index = null;
      if (predicate.getURI().equals(Euro5Constants.MAGIC_PROP_ELEMENT_TEXT_MATCH)) {
      	index  = euro5Service.rdcLabelIndex2(lang);
      } else {
      	assert(predicate.getURI().equals(Euro5Constants.MAGIC_PROP_SYMPTOM_TEXT_MATCH));
      	index = euro5Service.symptomLabelIndex2(lang);
      }
      */

      if (index == null) {
     		// Log.warn(this, "No index for " + predicate.getURI() + " and lang " + lang) ;
     		return IterLib.noResults(execCxt) ;
      }
      
      Var textMatchVar = (textMatch==null)?null:Var.alloc(textMatch) ;


      if ( match.isVariable() )
          // return varSubject(binding, Var.alloc(match), textMatchVar, searchString, index, execCxt) ;
      		return varSubject(binding, Var.alloc(match), textMatchVar, searchString, lang, index, execCxt) ;
      else
          return boundSubject(binding, match, searchString, index, execCxt) ;
    }
    
    private static boolean isValidSearchString(Node searchString)
    {
        if ( !searchString.isLiteral() )
        {
            // Log.warn(TextMatchMagicProp2_svg.class, "Not a string: "+searchString) ;
            return false ;
        }

        if ( searchString.getLiteralDatatypeURI() != null )
        {
            // Log.warn(TextMatchMagicProp2_svg.class, "Not a plain string: "+searchString) ;
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
                                    Var textMatchVar,
                                    String searchString,
                                    String lang,
                                    Index<ResourceLabelPair_SVG2> index,
                                    ExecutionContext execCxt)
    {
      	// Iterator<HitLARQ> iter = getIndex(execCxt).search(searchString) ;
    		Collection<ResourceLabelPair_SVG2> hits = index.searchText(searchString);

         
        HitConverter converter = new HitConverter(binding, match, textMatchVar, lang) ;
        
        // Iterator<Binding> iter2 = new Map1Iterator<HitLARQ, Binding>(converter, iter) ;
        Iterator<Binding> iter2 = new Map1Iterator<ResourceLabelPair_SVG2, Binding>(converter, hits.iterator()) ;
        QueryIterator qIter = new QueryIterPlainWrapper(iter2, execCxt) ;

        return qIter ;
    }
    
    static class HitConverter implements Map1<ResourceLabelPair_SVG2, Binding>
    {
        private Binding binding ;
        private Var match, foundLabelVar ;
        private String lang ;
        
        HitConverter(Binding binding, Var matchVar, Var foundLabelVar, String lang)
        {
            this.binding = binding ;
            this.match = matchVar ;
            this.foundLabelVar = foundLabelVar;
            this.lang = lang;
        }
        
        public Binding map1(ResourceLabelPair_SVG2 hit)
        {
        		// MOVING FROM JENA-ARQ 2.7.? TO 2.9.3 (under apache now), this doesn't compile anymore:
            /* Binding b = new BindingMap(binding) ;
            // b.add(match, hit.asNode()) 
            b.add(match, hit.getResource().asNode()) ; // MODE 1 */
            BindingMap b = BindingFactory.create(binding) ;
            // b.add(match, hit.asNode()) 
            b.add(match, hit.getResource().asNode()) ; // MODE 1
            // b.add(match, Node.createLiteral(hit.getLabel())) ; // tentative pour faire ?label ex:magic "sem web" // MODE 2. Ca a l'air de marcher, sauf que quand ?label trouvé est réutilisé ds
            // une autre pattern après, semble être comme non documenté (ne filtre pas). Don't know why, par ex:
            /* retourne ds ?x tous les labels de ?sym - comprends pas
            SELECT DISTINCT ?sym  ?x WHERE {
							?sym <http://www.w3.org/2000/01/rdf-schema#label> "Fumee blanche importante a l'echappement"@fr.
							?x <http://www.renault.com/euro5/schema#magic_symptomLabel> "fum"@fr.
							?sym <http://www.w3.org/2000/01/rdf-schema#label> ?x.
						}
						alors que
           SELECT DISTINCT ?sym  ?x WHERE {
							?sym <http://www.w3.org/2000/01/rdf-schema#label> "Fumee blanche importante a l'echappement"@fr.
							?sym <http://www.w3.org/2000/01/rdf-schema#label> ?x.
							?x <http://www.renault.com/euro5/schema#magic_symptomLabel> "fum"@fr.
						}
						est ok (mais là, c'est boundSubject qui est utilisé)
            */
            
            // v1.1.0.2: We MUST create the literal with a lang, or nasty bug
            // when using the magic prop in a query such as:
            /* SELECT ?res ?textMatch WHERE {
            	(?res ?textMatch) ex:myTextSearch "bougie".
            	?res rdfs.label ?textMatch.
            }*/
            // for one ?res, I get in ?textMatch all its rdfs.label (instead of the one found in the first line of query)
           if (foundLabelVar != null) b.add(foundLabelVar, Node.createLiteral(hit.getLabel(), lang, false));
           return b;
        }
        
    }
    
    public QueryIterator boundSubject(Binding binding, 
                                      Node match,
                                      String searchString,
                                      Index<ResourceLabelPair_SVG2> index,
                                      ExecutionContext execCxt)
    {
        // HitLARQ hit = getIndex(execCxt).contains(match, searchString) ;
     		Collection<ResourceLabelPair_SVG2> hits = index.searchText(searchString);
     		boolean found = false;

     		String matchUri = match.getURI(); // MODE 1
     		for(ResourceLabelPair_SVG2 resLabelPair : hits) {
     			String resUri = resLabelPair.getResource().getURI();
     			if (resUri.equals(matchUri)) {
     				found = true;
     				// foundLabel = resLabelPair.getLabel();
     				break;
     			}
     		}

     		/*String matchLabel = match.getLiteral().getLexicalForm(); // MODE 2
     		for(ResourceLabelPair resLabelPair : hits) {
      		if (resLabelPair.getLabel().equals(matchLabel)) {
     				found = true;
     				break;
     			}
     		}*/
     		
     		
        if ( !found )
            return new QueryIterNullIterator(execCxt) ;
        // return IterLib.oneResult(binding, score, NodeFactory.floatToNode(hit.getScore()), execCxt) ;
        return IterLib.result(binding, execCxt) ; ////// ???????????????????
    }

}
