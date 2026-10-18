package com.pawlife.app.core.data.repository

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.pawlife.app.BuildConfig
import com.pawlife.app.core.data.db.entity.PetEntity
import com.pawlife.app.core.domain.repository.GenerativeAiRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GenerativeAiRepositoryImpl @Inject constructor() : GenerativeAiRepository {

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY,
        generationConfig = generationConfig {
            temperature = 0.4f
        },
        systemInstruction = content {
            text("You are the PawLife AI Health Twin. You provide helpful triage and care suggestions for pets based on their profile. You must NEVER give absolute medical diagnoses. Always recommend consulting a vet for serious issues. Format your responses with bullet points and bold text for readability.")
        }
    )

    override fun generateHealthTriageStream(pet: PetEntity, symptoms: String): Flow<String> {
        if (BuildConfig.GEMINI_API_KEY == "dummy_gemini_key") {
            return kotlinx.coroutines.flow.flow {
                emit("This is a mock AI response because no Gemini API key is configured.\n\n")
                kotlinx.coroutines.delay(500)
                emit("If this were a real analysis, I would suggest the following based on the symptoms: ")
                emit(symptoms)
                emit("\n\n1. Monitor your pet closely.\n2. Keep them hydrated.\n3. Contact your vet if symptoms worsen.")
            }
        }

        val prompt = """
            Please analyze the following symptoms for my pet and provide a triage summary, along with 3 recommended questions I should ask my vet.
            
            Pet Context:
            - Name: ${pet.name}
            - Species: ${pet.species}
            - Breed: ${pet.breed}
            - Gender: ${pet.gender}
            - Weight: ${pet.weight} kg
            
            Symptoms / Question:
            $symptoms
        """.trimIndent()

        return generativeModel.generateContentStream(prompt).map { response ->
            response.text ?: ""
        }
    }

    override fun analyzePetImage(bitmap: android.graphics.Bitmap): Flow<String> {
        if (BuildConfig.GEMINI_API_KEY == "dummy_gemini_key") {
            return kotlinx.coroutines.flow.flow {
                emit("Mock Analysis: This looks like a healthy pet! ")
                kotlinx.coroutines.delay(500)
                emit("Please configure a real Gemini API key to get an actual analysis of this image.")
            }
        }

        val prompt = "Analyze this image of a pet. Identify the likely breed, any visible health issues, and general tips for caring for this type of pet."
        return generativeModel.generateContentStream(
            content {
                image(bitmap)
                text(prompt)
            }
        ).map { response ->
            response.text ?: ""
        }
    }

    override fun simulateWhatIfScenario(scenario: String): Flow<String> {
        if (BuildConfig.GEMINI_API_KEY == "dummy_gemini_key") {
            return kotlinx.coroutines.flow.flow {
                emit("Mock What-If Analysis: ")
                emit("If you do this, there might be short-term changes in energy and weight. ")
                emit("Please configure a real Gemini API key to unlock the full AI simulator.")
            }
        }

        val prompt = """
            You are a veterinary AI simulator. I will provide a hypothetical 'what-if' scenario regarding a pet's health, diet, or lifestyle.
            Analyze the potential short-term and long-term impacts on the pet's well-being.
            Provide a summary of expected changes in weight, energy, and health risks.
            
            Scenario: $scenario
        """.trimIndent()
        
        return generativeModel.generateContentStream(prompt).map { response ->
            response.text ?: ""
        }
    }

    override fun parseVetRecord(bitmap: android.graphics.Bitmap): Flow<String> {
        val prompt = """
            Please parse the following veterinary medical record or pet document from this image.
            Extract and summarize the key information such as:
            - Dates of Visit
            - Diagnoses or observations
            - Vaccinations given and due dates
            - Medications prescribed
            - Recommended follow-ups
            
            Format your response clearly with bullet points.
        """.trimIndent()
        
        return generativeModel.generateContentStream(
            content {
                image(bitmap)
                text(prompt)
            }
        ).map { response ->
            response.text ?: ""
        }
    }
}
