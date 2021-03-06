package wsc.ecj.gp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;

import wsc.data.pool.Service;
import wsc.graph.ServiceEdge;
import wsc.graph.ServiceInput;
import wsc.graph.ServiceOutput;
import wsc.graph.ServicePostcondition;
import wsc.graph.ServicePrecondition;
import wsc.owl.bean.OWLClass;

public class ServiceGPNode extends GPNode implements InOutNode {

	private static final long serialVersionUID = 1L;
	private Service service;

	private String serName;
	private List<ServiceInput> inputs;
	private List<ServiceOutput> outputs;
	private List<ServicePrecondition> preconditions;
	private List<ServicePostcondition> postconditions;
	private Set<ServiceEdge> semanticEdges;

	double maxTime =0.0;
	private Set<ServiceEdge> aggregatedServiceEdge;
	private List<Service> seenService;

	public ServiceGPNode() {
		children = new GPNode[0];
	}

	public ServiceGPNode(Set<ServiceEdge> semanticEdges) {
		children = new GPNode[0];
		this.setSemanticEdges(semanticEdges);
	}

	public String getSerName() {
		return serName;
	}

	public void setSerName(String serName) {
		this.serName = serName;
	}

	@Override
	public List<ServiceInput> getInputs() {
		return inputs;
	}

	public void setInputs(List<ServiceInput> inputs) {
		this.inputs = inputs;
	}

	@Override
	public List<ServiceOutput> getOutputs() {
		return outputs;
	}

	public void setOutputs(List<ServiceOutput> outputs) {
		this.outputs = outputs;
	}

	@Override
	public List<ServicePrecondition> getPreconditions() {
		return preconditions;
	}

	public void setPreconditions(List<ServicePrecondition> preconditions) {
		this.preconditions = preconditions;
	}

	@Override
	public List<ServicePostcondition> getPostconditions() {
		return postconditions;
	}

	public void setPostconditions(List<ServicePostcondition> postconditions) {
		this.postconditions = postconditions;
	}

	public Set<ServiceEdge> getSemanticEdges() {
		return semanticEdges;
	}

	public void setSemanticEdges(Set<ServiceEdge> semanticEdges) {
		this.semanticEdges = semanticEdges;
	}



	public double getMaxTime() {
		return maxTime;
	}

	public void setMaxTime(double maxTime) {
		this.maxTime = maxTime;
	}

	public Set<ServiceEdge> getAggregatedServiceEdge() {
		return aggregatedServiceEdge;
	}

	public void setAggregatedServiceEdge(Set<ServiceEdge> aggregatedServiceEdge) {
		this.aggregatedServiceEdge = aggregatedServiceEdge;
	}

	public List<Service> getSeenService() {
		return seenService;
	}

	public void setSeenService(List<Service> seenService) {
		this.seenService = seenService;
	}

	@Override
	public void eval(final EvolutionState state, final int thread, final GPData input, final ADFStack stack,
			final GPIndividual individual, final Problem problem) {

		WSCData rd = ((WSCData) (input));
		WSCInitializer init = (WSCInitializer) state.initializer;

		if (serName.equals("endNode")) {
			for (GPNode child : children) {
				child.eval(state, thread, input, stack, individual, problem);
			}
			rd.serName = serName;
			
			rd.seenServices = new ArrayList<Service>();
			rd.aggregatedServiceEdges = new HashSet<ServiceEdge>();
			
			
			serName = rd.serName;
			
			seenService = rd.seenServices;
			aggregatedServiceEdge = rd.aggregatedServiceEdges;
			
			
			// update for evaluation
			for (GPNode child : children) {
				ServiceGPNode childGPNode = (ServiceGPNode) child;
				//update MaxTime
				double childMaxTime = childGPNode.getMaxTime();

				if(children.length==1){
					maxTime += childMaxTime;					
				}else if(children.length>1){
					if (childMaxTime > maxTime)
						maxTime = childMaxTime;
				}
				
				//update seenService
				List<Service> childSeenService= childGPNode.getSeenService();
				if(childSeenService!=null){
					seenService.addAll(childSeenService);
				}
				//update aggregatedServiceEdge
				Set<ServiceEdge> childSemanticEdge = childGPNode.getAggregatedServiceEdge();
//				aggregatedServiceEdge.addAll(childSemanticEdge);
			}
			rd.maxTime = maxTime;
			maxTime = rd.maxTime;

		} else if (serName.equals("startNode")) {
			rd.serName = serName;
			rd.semanticEdges = this.semanticEdges;
			rd.aggregatedServiceEdges = this.semanticEdges;

			serName = rd.serName;
			semanticEdges = rd.semanticEdges;
			aggregatedServiceEdge = rd.aggregatedServiceEdges;
			
		} else {
			for (GPNode child : children) {
				child.eval(state, thread, input, stack, individual, problem);
			}
			Service service = WSCInitializer.serviceMap.get(serName);
			this.setService(service);
			rd.serName = serName;
			rd.maxTime = service.getQos()[WSCInitializer.TIME];
			rd.seenServices = new ArrayList<Service>();
			rd.seenServices.add(service);
			rd.inputs = service.getInputList();
			// set all isSatified of input to false
			for (ServiceInput serInput : rd.inputs) {
				serInput.setSatified(false);
			}

			rd.outputs = service.getOutputList();
			for (ServiceOutput serOutput : rd.outputs) {
				serOutput.setSatified(false);
			}

			// set all isSatified of input to false
			rd.preconditions = service.getPreconditionList();
			rd.postconditions = service.getPostconditionList();
			rd.semanticEdges = this.semanticEdges;
			rd.aggregatedServiceEdges = this.semanticEdges;

			// Store input and output information in this node
			serName = rd.serName;
			maxTime =rd.maxTime;
			seenService = rd.seenServices;
			inputs = rd.inputs;
			outputs = rd.outputs;
			preconditions = rd.preconditions;
			postconditions = rd.postconditions;
			semanticEdges = rd.semanticEdges;
			aggregatedServiceEdge = rd.aggregatedServiceEdges;


			// update overallsemanticEdge for evaluation
			for (GPNode child : children) {

				ServiceGPNode childGPNode = (ServiceGPNode) child;
				//update MaxTime
				double childMaxTime = childGPNode.getMaxTime();

				if(children.length==1){
					maxTime += childMaxTime;					
				}else if(children.length>1){
					if (childMaxTime > maxTime)
						maxTime = childMaxTime;
				}
				
				//update seenService
				List<Service> childSeenService= childGPNode.getSeenService();
				if(childSeenService!=null){
					seenService.addAll(childSeenService);
				}
				//update aggregatedServiceEdge
				Set<ServiceEdge> childSemanticEdge = childGPNode.getAggregatedServiceEdge();
//				aggregatedServiceEdge.addAll(childSemanticEdge);
			}

		}
	}

	// check there is inputs produced by the services Outputs or not
	private List isContainedOfromI(ServiceOutput serOutput, List<ServiceInput> overallInputs, WSCInitializer init,
			List<ServiceInput> overallInputsRemoved) {
		for (ServiceInput serInputs : overallInputs) {

			OWLClass givenClass = WSCInitializer.initialWSCPool.getSemanticsPool().getOwlClassHashMap()
					.get(WSCInitializer.initialWSCPool.getSemanticsPool().getOwlInstHashMap().get(serOutput.getOutput())
							.getRdfType().getResource().substring(1));
			OWLClass relatedClass = WSCInitializer.initialWSCPool.getSemanticsPool().getOwlClassHashMap()
					.get(WSCInitializer.initialWSCPool.getSemanticsPool().getOwlInstHashMap().get(serInputs.getInput())
							.getRdfType().getResource().substring(1));

			String a = givenClass.getID();
			String b = relatedClass.getID();

			while (true) {
				// Exact and PlugIn matching types
				if (givenClass.getID().equals(relatedClass.getID())) {
					overallInputsRemoved.add(serInputs);
					// return overallInputsRemoved;
				}
				if (givenClass.getSubClassOf() == null || givenClass.getSubClassOf().getResource().equals("")) {
					break;
				}
				givenClass = WSCInitializer.initialWSCPool.getSemanticsPool().getOwlClassHashMap()
						.get(givenClass.getSubClassOf().getResource().substring(1));
			}
		}
		return overallInputsRemoved;
	}

	public void setService(Service s) {
		service = s;
	}

	public Service getService() {
		return service;
	}

	@Override
	public String toString() {
		String serviceName;
		if (serName == null)
			serviceName = "null";
		else
			serviceName = serName;
		StringBuilder builder = new StringBuilder();
		builder.append(String.format("%d [label=\"%s\"]; ", hashCode(), serviceName));

		if (children != null) {
			for (int i = 0; i < children.length; i++) {
				GPNode child = children[i];
				if (child != null) {
					builder.append(String.format("%d -> %d [dir=back]; ", hashCode(), children[i].hashCode()));
					builder.append(children[i].toString());
				}
			}
		}

		return builder.toString();
	}

	@Override
	public int expectedChildren() {
		return 0;
	}

	@Override
	public int hashCode() {
		if (serName == null) {
			return "null".hashCode();
		}
		return super.hashCode();
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof ServiceGPNode) {
			ServiceGPNode o = (ServiceGPNode) other;
			return this.getSerName().equals(o.getSerName());
		} else
			return false;
	}

	// @Override
	// public ServiceGPNode clone() {
	// ServiceGPNode newNode = new ServiceGPNode();
	// newNode.setService(service);
	// newNode.inputs = inputs;
	// newNode.outputs = outputs;
	// return newNode;
	// }

}
