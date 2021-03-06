package uk.ac.ox.cs.pagoda.query;

import java.util.HashMap;
import java.util.Map;

import org.semanticweb.HermiT.model.Constant;
import org.semanticweb.HermiT.model.Individual;
import org.semanticweb.HermiT.model.Term;
import org.semanticweb.HermiT.model.Variable;

import uk.ac.ox.cs.JRDFox.JRDFStoreException;
import uk.ac.ox.cs.JRDFox.model.Datatype;
import uk.ac.ox.cs.JRDFox.model.GroundTerm;
import uk.ac.ox.cs.JRDFox.model.Literal;
import uk.ac.ox.cs.JRDFox.store.TupleIterator;
import uk.ac.ox.cs.pagoda.util.Namespace;

public class AnswerTuple {
	
	public static final String SEPARATOR = "\t";
	
	String m_str = null; 
	GroundTerm[] m_tuple; 

	public AnswerTuple(TupleIterator iter, int arity) {
		m_tuple = new GroundTerm[arity];
		try {
			for (int i = 0; i < arity; ++i)
				m_tuple[i] = iter.getGroundTerm(i);
		} catch (JRDFStoreException e) {
			e.printStackTrace();
		} 
	}
	
	public AnswerTuple(GroundTerm[] terms) {
		m_tuple = terms; 
	}
	
	private AnswerTuple(AnswerTuple sup, int arity) {
		m_tuple = new GroundTerm[arity]; 
		for (int i = 0; i < arity; ++i) m_tuple[i] = sup.m_tuple[i]; 
	}
	
	public int getArity() {
		return m_tuple.length; 
	}

	public int hashCode() {
//		return toString().hashCode();
		int code = 0; 
		for (int i = 0; i < m_tuple.length; ++i)
			code = code * 1997 + m_tuple[i].hashCode();
		return code; 
	}
	
	public boolean equals(Object obj) {
		if (!(obj instanceof AnswerTuple)) return false;
		AnswerTuple that = (AnswerTuple) obj; 
		if (m_tuple.length != that.m_tuple.length) return false; 
		for (int i = 0; i < m_tuple.length; ++i)
			if (!m_tuple[i].equals(that.m_tuple[i]))
				return false; 
		return true; 
//		return toString().equals(obj.toString()); 
	}
	
	public String toString() {
		if (m_str != null) return m_str; 
		StringBuilder sb = new StringBuilder();  
		for (int i = 0; i < m_tuple.length; ++i) {
			if (sb.length() != 0) sb.append(SEPARATOR);
			if (m_tuple[i] instanceof uk.ac.ox.cs.JRDFox.model.Individual)
				sb.append("<").append(((uk.ac.ox.cs.JRDFox.model.Individual) m_tuple[i]).getIRI()).append(">");
			else if (m_tuple[i] instanceof uk.ac.ox.cs.JRDFox.model.BlankNode) {
				sb.append(((uk.ac.ox.cs.JRDFox.model.BlankNode) m_tuple[i]).toString()); 
			}
			else {
				Literal l = (Literal) m_tuple[i]; 
				sb.append('"').append(l.getLexicalForm()).append("\"");
				if (!l.getDatatype().equals(Datatype.XSD_STRING) && !l.getDatatype().equals(Datatype.RDF_PLAIN_LITERAL))
					sb.append("^^<").append(l.getDatatype().getIRI()).append(">"); 
			}
		}
		return m_str = sb.toString();  
	}

	public GroundTerm getGroundTerm(int i) {
		return m_tuple[i]; 
	}
	
	public Map<Variable, Term> getAssignment(String[] vars) {
		Map<Variable, Term> map = new HashMap<Variable, Term>(); 
		int index = 0; 
		Term t; 
		for (String var: vars) {
			if (m_tuple[index] instanceof uk.ac.ox.cs.JRDFox.model.Individual) 
				t = Individual.create((((uk.ac.ox.cs.JRDFox.model.Individual) m_tuple[index]).getIRI())); 
			else {
				uk.ac.ox.cs.JRDFox.model.Literal l = (uk.ac.ox.cs.JRDFox.model.Literal) m_tuple[index]; 
				t = Constant.create(l.getLexicalForm(), l.getDatatype().getIRI()); 
			}
			map.put(Variable.create(var), t); 
			++index; 
		}
		return map; 
	}

	public boolean hasAuxPredicate() {
		String iri; 
		for (int i = 0; i < m_tuple.length; ++i)
			if ((m_tuple[i] instanceof uk.ac.ox.cs.JRDFox.model.Individual)) {
				iri = ((uk.ac.ox.cs.JRDFox.model.Individual) m_tuple[i]).getIRI();
				if ( iri.startsWith(Namespace.PAGODA_AUX) || iri.contains("_AUX") || iri.contains("_neg") || iri.contains("internal:def"))
					return true; 
			}
		return false; 
	}
	
	public boolean hasAnonyIndividual() {
		String iri; 
		for (int i = 0; i < m_tuple.length; ++i)
			if ((m_tuple[i] instanceof uk.ac.ox.cs.JRDFox.model.Individual)) {
				iri = ((uk.ac.ox.cs.JRDFox.model.Individual) m_tuple[i]).getIRI();
				if (iri.startsWith(Namespace.PAGODA_ANONY) || iri.startsWith(Namespace.KARMA_ANONY))
					return true; 
			}
		return false; 
	}

	public static AnswerTuple create(AnswerTuple extendedTuple, int length) {
		if (length == extendedTuple.getArity()) return extendedTuple;
		else return new AnswerTuple(extendedTuple, length); 
	}
	
}
