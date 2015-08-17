package uk.ac.ox.cs.pagoda.junit;

import static org.junit.Assert.*;

import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import uk.ac.ox.cs.pagoda.query.AnswerTuples;
import uk.ac.ox.cs.pagoda.reasoner.QueryReasoner;

public class CardinalityTest {

	@Test
	public void test() throws OWLOntologyCreationException {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		String iri = "http://www.example.org/test_cardinarlity.owl"; 
		OWLOntology ontology = manager.createOntology(IRI.create(iri)); 
		OWLDataFactory factory = manager.getOWLDataFactory(); 
		OWLClass A = factory.getOWLClass(IRI.create(iri + "#A")); 
		OWLObjectProperty r = factory.getOWLObjectProperty(IRI.create(iri + "#r")); 
		manager.addAxiom(ontology, factory.getOWLSubClassOfAxiom(A, factory.getOWLObjectExactCardinality(1, r))); 
		manager.addAxiom(ontology, factory.getOWLClassAssertionAxiom(A, factory.getOWLNamedIndividual(IRI.create(iri + "#a")))); 
		for (OWLAxiom axiom : ontology.getAxioms()) {
			System.out.println(axiom); 
		}
		
		QueryReasoner pagoda = null; 
		try {
			pagoda = QueryReasoner.getInstance(ontology);
			pagoda.loadOntology(ontology);
			if (!pagoda.preprocess()) 
				System.out.println("Inconsistent ontology.");
			String queryText = String.format("select ?x where {?x <%s#r> _:y .}", iri);
			System.out.println(queryText); 
			AnswerTuples tuples = pagoda.evaluate(queryText);
			assertTrue(tuples.isValid());
			while (tuples.isValid()) {
				System.out.println(tuples.getTuple()); 
				tuples.moveNext();
			}
		} finally {
			pagoda.dispose();
		}
	}

}
