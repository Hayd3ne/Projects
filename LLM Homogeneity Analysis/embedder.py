# embedder.py - with chunking and pooling
import sys
import json
import numpy as np # 
from sentence_transformers import SentenceTransformer

# Note: The model is loaded once here and reused across all text processing.
model = SentenceTransformer('all-mpnet-base-v2')

def get_long_text_embedding(long_text, model, max_chunk_size=250, overlap=50):
    """
    Generates a single, pooled (averaged) embedding for a long string by first 
    splitting it into overlapping chunks, embedding the chunks, and then averaging 
    the resulting vectors.
    
    Args:
        long_text (str): The input string (e.g., a document or large paragraph).
        model (SentenceTransformer): The loaded model.
        max_chunk_size (int): The approximate word limit for each text chunk.
        overlap (int): The number of words to overlap between sequential chunks.
        
    Returns:
        list: A list representing the pooled (averaged) embedding, ready for JSON serialization.
    """
    if not long_text:
        # Return a zero vector for empty string to maintain dimensionality
        return np.zeros(model.get_sentence_embedding_dimension()).tolist()

    # --- 1. Split the text into overlapping chunks (by word count) ---
    words = long_text.split()
    chunks = []
    
    start = 0
    # Loop to create chunks
    while start < len(words):
        end = min(start + max_chunk_size, len(words))
        chunk = " ".join(words[start:end])
        chunks.append(chunk)
        
        # Move the start position forward, compensating for overlap
        # Stop if the end of the last chunk was the end of the text
        if end == len(words):
            break
        
        start += (max_chunk_size - overlap)
        # Fail-safe break condition
        if start >= len(words) and end != len(words):
             break

    # --- 2. Embed each chunk ---
    # Convert to numpy for efficient pooling
    chunk_embeddings = model.encode(chunks, 
                                    convert_to_numpy=True, 
                                    show_progress_bar=False)
    
    # --- 3. Pool (Average) the embeddings ---
    # Calculate the mean vector across all chunk embeddings (axis=0)
    pooled_embedding = np.mean(chunk_embeddings, axis=0)
    
    return pooled_embedding.tolist() # Return as list for JSON serialization

def main(in_path, out_path):
    print(f"Loading texts from {in_path}...")
    with open(in_path, "r", encoding="utf-8") as f:
        texts = json.load(f)   # expect a list of strings

    if not isinstance(texts, list):
         print("Error: Input JSON must contain a list of strings.")
         sys.exit(1)

    print(f"Total {len(texts)} texts to process. Using chunking and pooling strategy.")
    final_embeddings = []
    
    # Process each long string using chunking and pooling
    for i, text in enumerate(texts):
        # Progress tracking for large lists
        if i % 100 == 0 and len(texts) > 100:
            print(f"Processing text {i+1} of {len(texts)}...")
            
        if not isinstance(text, str):
            print(f"Warning: Item {i} in the list is not a string ({type(text).__name__}). Using a zero vector placeholder.")
            embedding = np.zeros(model.get_sentence_embedding_dimension()).tolist()
        else:
            # Generate the single pooled embedding for the potentially long text
            embedding = get_long_text_embedding(text, model)
            
        final_embeddings.append(embedding)

    print("Encoding complete. Dumping results...")
    with open(out_path, "w", encoding="utf-8") as f:
        json.dump(final_embeddings, f)
    
    print(f"Successfully wrote {len(final_embeddings)} embeddings to {out_path}.")

if __name__ == "__main__":
    if len(sys.argv) < 3:
        print("Usage: python embedder.py input.json output.json")
        sys.exit(1)
    main(sys.argv[1], sys.argv[2])