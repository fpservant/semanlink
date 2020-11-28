/* Created on Nov 23, 2020 */
package net.semanlink.fps;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import net.semanlink.semanlink.DataLoader;
import net.semanlink.semanlink.SLDocCommentUpdate;
import net.semanlink.semanlink.SLDocument;
import net.semanlink.semanlink.SLModel;
import net.semanlink.semanlink.SLUtils;
import net.semanlink.semanlink.SLVocab;
import net.semanlink.sljena.JDocument;
import net.semanlink.util.FileUriFormat;

public class Comments2RelatedDocs_LoopUpdateTest { // 2020-11

@Test
public final void ATTENTION_MODIF_VRAI_SEMANLINK_FPS() throws Exception {
	SLModel m = DataLoader.fpsSLModel();
	String contextUrl = "http://127.0.0.1:8080/semanlink";
	// System.out.println(m.getModelUrl() + "\n" + contextUrl);
	assertTrue(contextUrl.equals(m.getModelUrl()));
	
	Iterator<SLDocument> docs = m.documents();
	int k = 0;
	int kchanges = 0;
	
	for (;docs.hasNext();) {
		SLDocument doc = docs.next();
		try {
			boolean changed = handleOneDoc((JDocument) doc, m, contextUrl);
			if (changed) {
				System.out.println(doc.getURI());
				kchanges++;
			}
		} catch (Exception e) {
			// exception on http://arxiv.org/pdf/cs.DS/0310019
			e.printStackTrace();
		}		
		k++;
		
		// if (kchanges > 5) break;
		// if (k > 100) break;
	}
}

// true if changed
private boolean handleOneDoc(JDocument doc, SLModel m, String contextUrl) {	
	// String comment = doc.getComment(); // zut, la langue
	Literal lit = getComment( doc.getRes());
	if (lit == null) return false;
	String comment = lit.getString();
	String lang = lit.getLanguage();

	// if (doc.getURI().equals("https://www2018.thewebconf.org/program/web-content-analysis/")) {
	return SLDocCommentUpdate.changeComment(m, doc, comment, lang, contextUrl);
}

//cf. JenaUtils
static public Literal getComment(Resource res) {
	Model m = res.getModel();
	NodeIterator ite = m.listObjectsOfProperty(res, m.getProperty(SLVocab.COMMENT_PROPERTY));
	Literal x = null;
	if (ite.hasNext()) {
		
		for (;ite.hasNext();) {
			RDFNode node = ite.nextNode();
			if (node instanceof Literal) {
				x = (Literal) node;
				if (ite.hasNext()) {
					throw new RuntimeException("several comments for " + res);
				}
			} else {
				throw new RuntimeException("not a literal comment " + res);
			}
		}
	}
	ite.close();
	return x;
}

//
//
//

// pb, parce que le doc redirige vers un autre page, et donc le click bookmarklet ensuite ne marche pad
@Test public final void test_x() throws Exception {
	SLModel m = DataLoader.fpsSLModel();
	String contextUrl = "http://127.0.0.1:8080/semanlink";
	String link = "https://doi.org/10.1145/3178876.3186007";
	String s = SLDocCommentUpdate.link2UriOfLinkedDocs(link, m, contextUrl);
	System.out.println(s);
}
}

// http://127.0.0.1:8080/semanlink/doc/?uri=https%3A%2F%2Fwww2018.thewebconf.org%2Fprogram%2Fweb-content-analysis%2F
// http://127.0.0.1:8080/semanlink/doc/2020/01/richer_sentence_embeddings_usin
/*
https://www.researchgate.net/publication/325251122_Patent_Document_Clustering_with_Deep_Embeddings
http://127.0.0.1:8080/semanlink/doc/2020/08/2003_11644_multi_label_text_c
https://arxiv.org/abs/1709.08568
http://127.0.0.1:8080/semanlink/doc/2019/12/_1905_11852_educe_explaining_
http://127.0.0.1:8080/semanlink/doc/2019/07/_1907_03950_learning_by_abstra
http://127.0.0.1:8080/semanlink/doc/2020/01/named_entity_recognition_with_b
http://127.0.0.1:8080/semanlink/doc/2020/06/representation_learning_for_inf
http://127.0.0.1:8080/semanlink/doc/2019/07/_1907_05242_large_memory_layer
http://127.0.0.1:8080/semanlink/doc/2020/01/investigating_entity_knowledge_
http://127.0.0.1:8080/semanlink/doc/2019/10/humans_store_about_1_5_megabyte_1
http://127.0.0.1:8080/semanlink/doc/2020/05/aakash_kumar_nain_sur_twitter_
https://aclweb.org/anthology/papers/C/C18/C18-1139/
http://127.0.0.1:8080/semanlink/doc/2020/06/1804_03235_large_scale_distri
http://127.0.0.1:8080/semanlink/doc/2020/10/classifying_documents_without_a
http://www.offconvex.org/2015/12/12/word-embeddings-1/
http://pyvandenbussche.info/2017/translating-embeddings-transe/
java.lang.RuntimeException: several comments for http://www.paulgraham.com/say.html
	at net.semanlink.fps.Comments2RelatedDocs_LoopUpdateTest.getComment(Comments2RelatedDocs_LoopUpdateTest.java:94)
	at net.semanlink.fps.Comments2RelatedDocs_LoopUpdateTest.handleOneDoc(Comments2RelatedDocs_LoopUpdateTest.java:73)
	at net.semanlink.fps.Comments2RelatedDocs_LoopUpdateTest.ATTENTION_MODIF_VRAI_SEMANLINK_FPS(Comments2RelatedDocs_LoopUpdateTest.java:54)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.lang.reflect.Method.invoke(Method.java:497)
	at org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:59)
	at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:12)
	at org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:56)
	at org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:17)
	at org.junit.runners.ParentRunner$3.evaluate(ParentRunner.java:306)
	at org.junit.runners.BlockJUnit4ClassRunner$1.evaluate(BlockJUnit4ClassRunner.java:100)
	at org.junit.runners.ParentRunner.runLeaf(ParentRunner.java:366)
	at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:103)
	at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:63)
	at org.junit.runners.ParentRunner$4.run(ParentRunner.java:331)
	at org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:79)
	at org.junit.runners.ParentRunner.runChildren(ParentRunner.java:329)
	at org.junit.runners.ParentRunner.access$100(ParentRunner.java:66)
	at org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:293)
	at org.junit.runners.ParentRunner$3.evaluate(ParentRunner.java:306)
	at org.junit.runners.ParentRunner.run(ParentRunner.java:413)
	at org.eclipse.jdt.internal.junit4.runner.JUnit4TestReference.run(JUnit4TestReference.java:86)
	at org.eclipse.jdt.internal.junit.runner.TestExecution.run(TestExecution.java:38)
	at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.runTests(RemoteTestRunner.java:538)
	at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.runTests(RemoteTestRunner.java:760)
	at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.run(RemoteTestRunner.java:460)
	at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.main(RemoteTestRunner.java:206)
http://www.offconvex.org/2019/03/19/CURL/
https://towardsdatascience.com/how-to-do-deep-learning-on-graphs-with-graph-convolutional-networks-7d2250723780
http://127.0.0.1:8080/semanlink/doc/2020/06/approximating_the_softmax_for_l
http://127.0.0.1:8080/semanlink/doc/2019/12/unsupervised_learning_with_text
http://127.0.0.1:8080/semanlink/doc/2020/06/google_ai_blog_extracting_stru
http://127.0.0.1:8080/semanlink/doc/2020/03/chaitanya_joshi_sur_twitter_
https://openreview.net/forum?id=SyK00v5xx
http://127.0.0.1:8080/semanlink/doc/2019/07/finding_similar_quora_questions
http://127.0.0.1:8080/semanlink/doc/2020/05/you_can_teach_an_old_dog_new_tr
http://127.0.0.1:8080/semanlink/doc/2020/01/elasticsearch_meets_bert_build
http://127.0.0.1:8080/semanlink/doc/2020/07/2002_10640_differentiable_rea
https://arxiv.org/abs/1801.06146
https://github.com/zalandoresearch/flair
http://ruder.io/word-embeddings-2017/
http://127.0.0.1:8080/semanlink/doc/2020/04/turning_up_the_heat_the_mechan
https://esc.fnwi.uva.nl/thesis/centraal/files/f1554608041.pdf
http://127.0.0.1:8080/semanlink/doc/2019/09/jade_abbott_sur_twitter_call
http://127.0.0.1:8080/semanlink/doc/2020/08/the_extreme_classification_repo
http://127.0.0.1:8080/semanlink/doc/2020/03/_2003_02320_knowledge_graphs
http://127.0.0.1:8080/semanlink/doc/2020/07/end_to_end_learning_with_text_
https://arxiv.org/abs/1601.01343
https://arxiv.org/pdf/1004.5370.pdf
http://127.0.0.1:8080/semanlink/doc/2019/07/sofie_van_landeghem_entity_lin_1
http://127.0.0.1:8080/semanlink/doc/2019/10/these_modeles_neuronaux_pour_
https://www.youtube.com/watch?v=nFCxTtBqF5U
http://127.0.0.1:8080/semanlink/doc/2020/07/how_to_use_bert_for_finding_sim
https://dl.acm.org/citation.cfm?doid=3184558.3186906
http://127.0.0.1:8080/semanlink/doc/2019/09/evolution_of_representations_in
https://nlp.stanford.edu/IR-book/html/htmledition/pseudo-relevance-feedback-1.html
http://127.0.0.1:8080/semanlink/doc/2019/08/product_key_memory_pkm_minima
http://127.0.0.1:8080/semanlink/doc/2020/07/2007_00849_facts_as_experts_
https://arxiv.org/abs/1807.06036
http://127.0.0.1:8080/semanlink/doc/2020/02/adam_roberts_sur_twitter_new
http://127.0.0.1:8080/semanlink/doc/2020/04/deepak_nathani_%7C_pay_attention_
http://127.0.0.1:8080/semanlink/doc/2020/01/training_a_speaker_embedding_fr
http://127.0.0.1:8080/semanlink/doc/2019/06/language_trees_and_geometry_i
http://www.jneurosci.org/content/38/44/9563
http://127.0.0.1:8080/semanlink/doc/2020/02/how_much_knowledge_can_you_pack
https://www2018.thewebconf.org/program/tutorials-track/tutorial-213/
java.lang.RuntimeException: not a literal comment http://www.petrikainulainen.net/programming/maven/running-solr-with-maven/
	at net.semanlink.fps.Comments2RelatedDocs_LoopUpdateTest.getComment(Comments2RelatedDocs_LoopUpdateTest.java:97)
	at net.semanlink.fps.Comments2RelatedDocs_LoopUpdateTest.handleOneDoc(Comments2RelatedDocs_LoopUpdateTest.java:73)
	at net.semanlink.fps.Comments2RelatedDocs_LoopUpdateTest.ATTENTION_MODIF_VRAI_SEMANLINK_FPS(Comments2RelatedDocs_LoopUpdateTest.java:54)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.lang.reflect.Method.invoke(Method.java:497)
	at org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:59)
	at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:12)
	at org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:56)
	at org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:17)
	at org.junit.runners.ParentRunner$3.evaluate(ParentRunner.java:306)
	at org.junit.runners.BlockJUnit4ClassRunner$1.evaluate(BlockJUnit4ClassRunner.java:100)
	at org.junit.runners.ParentRunner.runLeaf(ParentRunner.java:366)
	at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:103)
	at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:63)
	at org.junit.runners.ParentRunner$4.run(ParentRunner.java:331)
	at org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:79)
	at org.junit.runners.ParentRunner.runChildren(ParentRunner.java:329)
	at org.junit.runners.ParentRunner.access$100(ParentRunner.java:66)
	at org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:293)
	at org.junit.runners.ParentRunner$3.evaluate(ParentRunner.java:306)
	at org.junit.runners.ParentRunner.run(ParentRunner.java:413)
	at org.eclipse.jdt.internal.junit4.runner.JUnit4TestReference.run(JUnit4TestReference.java:86)
	at org.eclipse.jdt.internal.junit.runner.TestExecution.run(TestExecution.java:38)
	at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.runTests(RemoteTestRunner.java:538)
	at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.runTests(RemoteTestRunner.java:760)
	at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.run(RemoteTestRunner.java:460)
	at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.main(RemoteTestRunner.java:206)
http://127.0.0.1:8080/semanlink/doc/2019/04/earia_report.pdf
http://acl2014.org/acl2014/P14-1/pdf/P14-1119.pdf
https://pdfs.semanticscholar.org/e398/d9d7e090a8d6f906b5da59925da212f6bc51.pdf
https://towardsdatascience.com/how-to-do-deep-learning-on-graphs-with-graph-convolutional-networks-62acf5b143d0
http://127.0.0.1:8080/semanlink/doc/2020/05/a_knowledge_graph_embedding_lib
http://127.0.0.1:8080/semanlink/doc/2020/04/2001_09522_taxoexpan_self_su
java.lang.RuntimeException: not a literal comment http://easy2use.renault.fr/Fiche.aspx?fiche=23
	at net.semanlink.fps.Comments2RelatedDocs_LoopUpdateTest.getComment(Comments2RelatedDocs_LoopUpdateTest.java:97)
	at net.semanlink.fps.Comments2RelatedDocs_LoopUpdateTest.handleOneDoc(Comments2RelatedDocs_LoopUpdateTest.java:73)
	at net.semanlink.fps.Comments2RelatedDocs_LoopUpdateTest.ATTENTION_MODIF_VRAI_SEMANLINK_FPS(Comments2RelatedDocs_LoopUpdateTest.java:54)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.lang.reflect.Method.invoke(Method.java:497)
	at org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:59)
	at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:12)
	at org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:56)
	at org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:17)
	at org.junit.runners.ParentRunner$3.evaluate(ParentRunner.java:306)
	at org.junit.runners.BlockJUnit4ClassRunner$1.evaluate(BlockJUnit4ClassRunner.java:100)
	at org.junit.runners.ParentRunner.runLeaf(ParentRunner.java:366)
	at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:103)
	at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:63)
	at org.junit.runners.ParentRunner$4.run(ParentRunner.java:331)
	at org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:79)
	at org.junit.runners.ParentRunner.runChildren(ParentRunner.java:329)
	at org.junit.runners.ParentRunner.access$100(ParentRunner.java:66)
	at org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:293)
	at org.junit.runners.ParentRunner$3.evaluate(ParentRunner.java:306)
	at org.junit.runners.ParentRunner.run(ParentRunner.java:413)
	at org.eclipse.jdt.internal.junit4.runner.JUnit4TestReference.run(JUnit4TestReference.java:86)
	at org.eclipse.jdt.internal.junit.runner.TestExecution.run(TestExecution.java:38)
	at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.runTests(RemoteTestRunner.java:538)
	at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.runTests(RemoteTestRunner.java:760)
	at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.run(RemoteTestRunner.java:460)
	at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.main(RemoteTestRunner.java:206)
https://arxiv.org/abs/1811.06031
http://127.0.0.1:8080/semanlink/doc/2019/07/mining_quality_phrases_from_mas
http://127.0.0.1:8080/semanlink/doc/2020/10/1911_11506_word_class_embeddi
http://127.0.0.1:8080/semanlink/doc/2019/11/elasticsearch_rss_feed_indexer_
https://drive.google.com/file/d/15ehMIJ7wY9A7RSmyJPNmrBMuC7se0PMP/view
http://www.offconvex.org/2016/02/14/word-embeddings-2/
http://127.0.0.1:8080/semanlink/doc/2020/09/le_langage_une_emergence_explo
http://127.0.0.1:8080/semanlink/doc/2020/04/a_comprehensive_survey_of_knowl
https://github.com/kawine/usif
http://127.0.0.1:8080/semanlink/doc/2019/06/_1812_00417_snorkel_drybell_a
http://127.0.0.1:8080/semanlink/doc/2019/08/_1908_10084_sentence_bert_sen
http://127.0.0.1:8080/semanlink/doc/2019/06/a_structural_probe_for_finding_
http://127.0.0.1:8080/semanlink/doc/2017/09/folderIdeaTest/
http://127.0.0.1:8080/semanlink/doc/2018/11/adapting_the_neural_encoder_dec.jpg
https://guillaumegenthial.github.io/sequence-tagging-with-tensorflow.html
http://127.0.0.1:8080/semanlink/doc/2019/10/textual_representation_learning
http://127.0.0.1:8080/semanlink/doc/2020/05/1706_00384_deep_mutual_learni
https://franceisai.com/conferences/conference-2018
https://www.youtube.com/watch?v=KR46z_V0BVw
http://127.0.0.1:8080/semanlink/doc/2019/05/AI%20for%20Engineering.md
http://127.0.0.1:8080/semanlink/doc/2019/10/feature_wise_transformations
http://mostafadehghani.com/2017/04/23/beating-the-teacher-neural-ranking-models-with-weak-supervision/
http://127.0.0.1:8080/semanlink/doc/2019/07/_1602_01137_a_dual_embedding_s
http://127.0.0.1:8080/semanlink/doc/2020/05/peter_bloem_sur_twitter_one_
http://127.0.0.1:8080/semanlink/doc/2020/09/representing_text_for_joint_emb
http://127.0.0.1:8080/semanlink/doc/2019/10/language_and_perception_in_deep
http://127.0.0.1:8080/semanlink/doc/2020/02/_1911_05507_compressive_transf
http://127.0.0.1:8080/semanlink/doc/2020/06/information_bottleneck_for_nlp_
https://www.offconvex.org/2016/07/10/embeddingspolysemy/
http://emnlp2014.org/papers/pdf/EMNLP2014167.pdf
https://www.youtube.com/watch?v=Yr1mOzC93xs
http://127.0.0.1:8080/semanlink/doc/2018/09/bengio/
java.lang.RuntimeException: several comments for http://bibd.uni-giessen.de/gdoc/2002/uni/d020057.pdf
	at net.semanlink.fps.Comments2RelatedDocs_LoopUpdateTest.getComment(Comments2RelatedDocs_LoopUpdateTest.java:94)
	at net.semanlink.fps.Comments2RelatedDocs_LoopUpdateTest.handleOneDoc(Comments2RelatedDocs_LoopUpdateTest.java:73)
	at net.semanlink.fps.Comments2RelatedDocs_LoopUpdateTest.ATTENTION_MODIF_VRAI_SEMANLINK_FPS(Comments2RelatedDocs_LoopUpdateTest.java:54)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.lang.reflect.Method.invoke(Method.java:497)
	at org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:59)
	at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:12)
	at org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:56)
	at org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:17)
	at org.junit.runners.ParentRunner$3.evaluate(ParentRunner.java:306)
	at org.junit.runners.BlockJUnit4ClassRunner$1.evaluate(BlockJUnit4ClassRunner.java:100)
	at org.junit.runners.ParentRunner.runLeaf(ParentRunner.java:366)
	at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:103)
	at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:63)
	at org.junit.runners.ParentRunner$4.run(ParentRunner.java:331)
	at org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:79)
	at org.junit.runners.ParentRunner.runChildren(ParentRunner.java:329)
	at org.junit.runners.ParentRunner.access$100(ParentRunner.java:66)
	at org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:293)
	at org.junit.runners.ParentRunner$3.evaluate(ParentRunner.java:306)
	at org.junit.runners.ParentRunner.run(ParentRunner.java:413)
	at org.eclipse.jdt.internal.junit4.runner.JUnit4TestReference.run(JUnit4TestReference.java:86)
	at org.eclipse.jdt.internal.junit.runner.TestExecution.run(TestExecution.java:38)
	at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.runTests(RemoteTestRunner.java:538)
	at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.runTests(RemoteTestRunner.java:760)
	at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.run(RemoteTestRunner.java:460)
	at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.main(RemoteTestRunner.java:206)
http://nadbordrozd.github.io/blog/2016/05/20/text-classification-with-word2vec/
http://127.0.0.1:8080/semanlink/doc/2019/10/paris_nlp_season_4_meetup_1_at
http://127.0.0.1:8080/semanlink/doc/2020/02/_1703_07464_no_fuss_distance_m
http://127.0.0.1:8080/semanlink/doc/2019/06/google_ai_blog_harnessing_orga
http://127.0.0.1:8080/semanlink/doc/2019/10/NLP-%20interesting%20papers%20-%20ideas.md
http://nlp.town/blog/sentence-similarity/
https://doi.org/10.1145/3178876.3186007
http://papers.nips.cc/paper/5071-translating-embeddings-for-modeling-multi-rela
https://wikipedia2vec.github.io/wikipedia2vec/
http://127.0.0.1:8080/semanlink/doc/2020/01/interpretable_named_entity_reco
http://127.0.0.1:8080/semanlink/doc/2020/02/canwen_xu_sur_twitter_wtf_w
http://127.0.0.1:8080/semanlink/doc/2019/06/_1811_09386_explicit_interacti
http://127.0.0.1:8080/semanlink/doc/2019/11/meetup_paris_40_beyond_plain
http://127.0.0.1:8080/semanlink/doc/2019/08/reasoning_with_neural_tensor_ne
http://www.aclweb.org/anthology/Q16-1028
https://pdfs.semanticscholar.org/873e/ea884de581f79b1e783052f8e9fa60726fc8.pdf
http://127.0.0.1:8080/semanlink/doc/2019/05/AI%20for%20Engineering%20Seminar%20v2.pdf
https://github.com/nlptown/nlp-notebooks/blob/master/Simple%20Sentence%20Similarity.ipynb
http://127.0.0.1:8080/semanlink/doc/2019/06/_1905_10070_label_aware_docume
http://blog.aylien.com/a-review-of-the-recent-history-of-natural-language-processing/
http://cap2018.litislab.fr/slides_AB.pdf
https://arxiv.org/abs/1704.08803
http://www.offconvex.org/2018/06/17/textembeddings/
http://127.0.0.1:8080/semanlink/doc/2020/07/Re_%20un%20article%20int%C3%A9ressant.rtf
http://127.0.0.1:8080/semanlink/doc/2020/04/contrastive_predictive_coding
http://aclweb.org/anthology/C18-1139
http://127.0.0.1:8080/semanlink/doc/2020/05/confinement_du_12_mars_au_pre
http://127.0.0.1:8080/semanlink/doc/2020/01/building_a_search_engine_with_b
http://127.0.0.1:8080/semanlink/doc/2020/02/a_new_model_and_dataset_for_lon
http://127.0.0.1:8080/semanlink/doc/2019/09/_1909_01380_the_bottom_up_evol
https://medium.com/dair-ai/hmtl-multi-task-learning-for-state-of-the-art-nlp-245572bbb601
http://deliprao.com/archives/262
http://www.aclweb.org/anthology/W18-3012
http://127.0.0.1:8080/semanlink/doc/2020/03/chengkai_li_sur_twitter_link
https://github.com/UKPLab/emnlp2017-bilstm-cnn-crf
https://nlpparis.files.wordpress.com/2018/09/talk_meetup_nlp_guillaume_lample.pdf
http://127.0.0.1:8080/semanlink/doc/2020/06/1910_00163_specializing_word_
http://cidrdb.org/cidr2019/papers/p117-kraska-cidr19.pdf
http://127.0.0.1:8080/semanlink/doc/2020/01/test_md
http://127.0.0.1:8080/semanlink/doc/2019/02/jeremy_howard_on_twitter_such
http://127.0.0.1:8080/semanlink/doc/2020/07/2007_00077_similarity_search_
http://www.lix.polytechnique.fr/~mvazirg/gow_tutorial_webconf_2018.pdf
http://unsupervised.cs.princeton.edu/ICMtalk/aroraplenary.html
http://127.0.0.1:8080/semanlink/doc/2019/07/_1907_07355_probing_neural_net
http://127.0.0.1:8080/semanlink/doc/2020/05/1511_03643_unifying_distillat
http://127.0.0.1:8080/semanlink/doc/2020/07/ukplab_sentence_transformers_s
http://www.hlt.utdallas.edu/~vince/papers/coling10-keyphrase.pdf
https://arxiv.org/abs/1810.00438
http://127.0.0.1:8080/semanlink/doc/2018/04/TR_%20%20Pr%C3%A9sentation%20du%20DATALAKE%20ET%20ELASTIC%20chez%20Renault.rtf
http://127.0.0.1:8080/semanlink/doc/2020/03/transformers_are_graph_neural_n
java.lang.RuntimeException: several comments for http://127.0.0.1:8080/semanlink/doc/2006/03/Une%20%C3%A9tude%20de%20lOCDE%20conclut%20%C3%A0%20l.htm
	at net.semanlink.fps.Comments2RelatedDocs_LoopUpdateTest.getComment(Comments2RelatedDocs_LoopUpdateTest.java:94)
	at net.semanlink.fps.Comments2RelatedDocs_LoopUpdateTest.handleOneDoc(Comments2RelatedDocs_LoopUpdateTest.java:73)
	at net.semanlink.fps.Comments2RelatedDocs_LoopUpdateTest.ATTENTION_MODIF_VRAI_SEMANLINK_FPS(Comments2RelatedDocs_LoopUpdateTest.java:54)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.lang.reflect.Method.invoke(Method.java:497)
	at org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:59)
	at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:12)
	at org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:56)
	at org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:17)
	at org.junit.runners.ParentRunner$3.evaluate(ParentRunner.java:306)
	at org.junit.runners.BlockJUnit4ClassRunner$1.evaluate(BlockJUnit4ClassRunner.java:100)
	at org.junit.runners.ParentRunner.runLeaf(ParentRunner.java:366)
	at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:103)
	at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:63)
	at org.junit.runners.ParentRunner$4.run(ParentRunner.java:331)
	at org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:79)
	at org.junit.runners.ParentRunner.runChildren(ParentRunner.java:329)
	at org.junit.runners.ParentRunner.access$100(ParentRunner.java:66)
	at org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:293)
	at org.junit.runners.ParentRunner$3.evaluate(ParentRunner.java:306)
	at org.junit.runners.ParentRunner.run(ParentRunner.java:413)
	at org.eclipse.jdt.internal.junit4.runner.JUnit4TestReference.run(JUnit4TestReference.java:86)
	at org.eclipse.jdt.internal.junit.runner.TestExecution.run(TestExecution.java:38)
	at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.runTests(RemoteTestRunner.java:538)
	at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.runTests(RemoteTestRunner.java:760)
	at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.run(RemoteTestRunner.java:460)
	at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.main(RemoteTestRunner.java:206)
http://127.0.0.1:8080/semanlink/doc/2019/04/Rapport%20EARIA.rtf
https://nlp.stanford.edu/manning/talks/Simons-Institute-Manning-2017.pdf
https://ai.stanford.edu/blog/weak-supervision/
http://www4.cnrs-dir.fr/insb/recherche/parutions/articles2018/b-cottereau.html
java.lang.RuntimeException: several comments for http://paolo.evectors.it/stories/entKcollectorWWWW.html
	at net.semanlink.fps.Comments2RelatedDocs_LoopUpdateTest.getComment(Comments2RelatedDocs_LoopUpdateTest.java:94)
	at net.semanlink.fps.Comments2RelatedDocs_LoopUpdateTest.handleOneDoc(Comments2RelatedDocs_LoopUpdateTest.java:73)
	at net.semanlink.fps.Comments2RelatedDocs_LoopUpdateTest.ATTENTION_MODIF_VRAI_SEMANLINK_FPS(Comments2RelatedDocs_LoopUpdateTest.java:54)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.lang.reflect.Method.invoke(Method.java:497)
	at org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:59)
	at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:12)
	at org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:56)
	at org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:17)
	at org.junit.runners.ParentRunner$3.evaluate(ParentRunner.java:306)
	at org.junit.runners.BlockJUnit4ClassRunner$1.evaluate(BlockJUnit4ClassRunner.java:100)
	at org.junit.runners.ParentRunner.runLeaf(ParentRunner.java:366)
	at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:103)
	at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:63)
	at org.junit.runners.ParentRunner$4.run(ParentRunner.java:331)
	at org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:79)
	at org.junit.runners.ParentRunner.runChildren(ParentRunner.java:329)
	at org.junit.runners.ParentRunner.access$100(ParentRunner.java:66)
	at org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:293)
	at org.junit.runners.ParentRunner$3.evaluate(ParentRunner.java:306)
	at org.junit.runners.ParentRunner.run(ParentRunner.java:413)
	at org.eclipse.jdt.internal.junit4.runner.JUnit4TestReference.run(JUnit4TestReference.java:86)
	at org.eclipse.jdt.internal.junit.runner.TestExecution.run(TestExecution.java:38)
	at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.runTests(RemoteTestRunner.java:538)
	at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.runTests(RemoteTestRunner.java:760)
	at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.run(RemoteTestRunner.java:460)
	at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.main(RemoteTestRunner.java:206)
http://127.0.0.1:8080/semanlink/doc/2019/06/kawin_ethayarajh_sur_twitter_
http://127.0.0.1:8080/semanlink/doc/2020/11/the_unreasonable_syntactic_expr
https://arxiv.org/abs/1601.03764
http://127.0.0.1:8080/semanlink/doc/2020/09/1909_01259_neural_attentive_b
http://127.0.0.1:8080/semanlink/doc/2020/10/guillaume_lample_sur_twitter_
https://openreview.net/forum?id=rJedbn0ctQ
http://127.0.0.1:8080/semanlink/doc/2020/11/2010_03496_inductive_entity_r
http://127.0.0.1:8080/semanlink/doc/2019/02/W3C%20Workshop%20on%20%22Web%20Standardization%20for%20Graph%20Data...%22.md
http://127.0.0.1:8080/semanlink/doc/2020/04/1906_01195_learning_attention
http://127.0.0.1:8080/semanlink/doc/2019/05/robust_language_representation_
http://127.0.0.1:8080/semanlink/doc/2020/06/on_word_embeddings
https://dl.acm.org/citation.cfm?id=3159660
*/