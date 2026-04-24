package fr.karabodjan.jarvis.service;

import fr.karabodjan.jarvis.model.Agent;

public interface IAgentService {

    AgentTask launch(Agent agent);
}