import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import TutorialsList from '../tutorials-list.component';
import TutorialDataService from '../../services/tutorial.service';

// Mock the tutorial service
jest.mock('../../services/tutorial.service');

/**
 * Comprehensive test suite for TutorialsList component
 * Tests FIX #2 (status display) and FIX #4 (index selection)
 */
describe('TutorialsList Component', () => {
  
  const mockTutorials = [
    { id: 1, title: 'Tutorial 1', description: 'Desc 1', published: true },
    { id: 2, title: 'Tutorial 2', description: 'Desc 2', published: false },
    { id: 3, title: 'Tutorial 3', description: 'Desc 3', published: true }
  ];

  beforeEach(() => {
    // Reset mocks before each test
    jest.clearAllMocks();
    
    // Default mock implementation
    TutorialDataService.getAll.mockResolvedValue({ data: mockTutorials });
    TutorialDataService.findByTitle.mockResolvedValue({ data: [] });
    TutorialDataService.deleteAll.mockResolvedValue({ data: {} });
  });

  const renderComponent = () => {
    return render(
      <BrowserRouter>
        <TutorialsList />
      </BrowserRouter>
    );
  };

  // ========== FIX #2: Status display tests ==========

  test('FIX #2: should display "Published" when tutorial.published is true', async () => {
    renderComponent();

    // Wait for tutorials to load
    await waitFor(() => {
      expect(screen.getByText('Tutorial 1')).toBeInTheDocument();
    });

    // Click on first tutorial (published=true)
    fireEvent.click(screen.getByText('Tutorial 1'));

    // Should display "Published" (not "Pending")
    await waitFor(() => {
      expect(screen.getByText('Published')).toBeInTheDocument();
      expect(screen.queryByText('Pending')).not.toBeInTheDocument();
    });
  });

  test('FIX #2: should display "Pending" when tutorial.published is false', async () => {
    renderComponent();

    await waitFor(() => {
      expect(screen.getByText('Tutorial 2')).toBeInTheDocument();
    });

    // Click on second tutorial (published=false)
    fireEvent.click(screen.getByText('Tutorial 2'));

    // Should display "Pending" (not "Published")
    await waitFor(() => {
      expect(screen.getByText('Pending')).toBeInTheDocument();
      expect(screen.queryByText('Published')).not.toBeInTheDocument();
    });
  });

  test('FIX #2: should correctly toggle between Published and Pending', async () => {
    renderComponent();

    await waitFor(() => {
      expect(screen.getByText('Tutorial 1')).toBeInTheDocument();
    });

    // Click published tutorial
    fireEvent.click(screen.getByText('Tutorial 1'));
    await waitFor(() => {
      expect(screen.getByText('Published')).toBeInTheDocument();
    });

    // Click unpublished tutorial
    fireEvent.click(screen.getByText('Tutorial 2'));
    await waitFor(() => {
      expect(screen.getByText('Pending')).toBeInTheDocument();
    });

    // Click published tutorial again
    fireEvent.click(screen.getByText('Tutorial 3'));
    await waitFor(() => {
      expect(screen.getByText('Published')).toBeInTheDocument();
    });
  });

  // ========== FIX #4: Index selection tests ==========

  test('FIX #4: should highlight first tutorial (index 0) when clicked', async () => {
    renderComponent();

    await waitFor(() => {
      expect(screen.getByText('Tutorial 1')).toBeInTheDocument();
    });

    const firstItem = screen.getByText('Tutorial 1').closest('li');
    
    // Click first tutorial
    fireEvent.click(screen.getByText('Tutorial 1'));

    // First item should have 'active' class
    await waitFor(() => {
      expect(firstItem).toHaveClass('active');
    });
  });

  test('FIX #4: should highlight second tutorial (index 1) when clicked', async () => {
    renderComponent();

    await waitFor(() => {
      expect(screen.getByText('Tutorial 2')).toBeInTheDocument();
    });

    const secondItem = screen.getByText('Tutorial 2').closest('li');
    
    // Click second tutorial
    fireEvent.click(screen.getByText('Tutorial 2'));

    // Second item should have 'active' class
    await waitFor(() => {
      expect(secondItem).toHaveClass('active');
    });
  });

  test('FIX #4: should move active class when clicking different tutorials', async () => {
    renderComponent();

    await waitFor(() => {
      expect(screen.getByText('Tutorial 1')).toBeInTheDocument();
    });

    const firstItem = screen.getByText('Tutorial 1').closest('li');
    const secondItem = screen.getByText('Tutorial 2').closest('li');

    // Click first tutorial
    fireEvent.click(screen.getByText('Tutorial 1'));
    await waitFor(() => {
      expect(firstItem).toHaveClass('active');
      expect(secondItem).not.toHaveClass('active');
    });

    // Click second tutorial
    fireEvent.click(screen.getByText('Tutorial 2'));
    await waitFor(() => {
      expect(firstItem).not.toHaveClass('active');
      expect(secondItem).toHaveClass('active');
    });
  });

  test('FIX #4: should correctly set currentIndex for all tutorials', async () => {
    renderComponent();

    await waitFor(() => {
      expect(screen.getByText('Tutorial 1')).toBeInTheDocument();
    });

    // Test each tutorial
    for (let i = 0; i < mockTutorials.length; i++) {
      const tutorial = mockTutorials[i];
      const listItem = screen.getByText(tutorial.title).closest('li');

      fireEvent.click(screen.getByText(tutorial.title));

      await waitFor(() => {
        expect(listItem).toHaveClass('active');
      });
    }
  });

  // ========== Additional functionality tests ==========

  test('should load and display tutorials on mount', async () => {
    renderComponent();

    await waitFor(() => {
      expect(screen.getByText('Tutorial 1')).toBeInTheDocument();
      expect(screen.getByText('Tutorial 2')).toBeInTheDocument();
      expect(screen.getByText('Tutorial 3')).toBeInTheDocument();
    });

    expect(TutorialDataService.getAll).toHaveBeenCalledTimes(1);
  });

  test('should search tutorials by title', async () => {
    const searchResults = [mockTutorials[0]];
    TutorialDataService.findByTitle.mockResolvedValue({ data: searchResults });

    renderComponent();

    await waitFor(() => {
      expect(screen.getByText('Tutorial 1')).toBeInTheDocument();
    });

    // Enter search term
    const searchInput = screen.getByPlaceholderText('Search by title');
    fireEvent.change(searchInput, { target: { value: 'Tutorial 1' } });

    // Click search button
    const searchButton = screen.getByText('Search');
    fireEvent.click(searchButton);

    await waitFor(() => {
      expect(TutorialDataService.findByTitle).toHaveBeenCalledWith('Tutorial 1');
    });
  });

  test('should display tutorial details when selected', async () => {
    renderComponent();

    await waitFor(() => {
      expect(screen.getByText('Tutorial 1')).toBeInTheDocument();
    });

    // Click tutorial
    fireEvent.click(screen.getByText('Tutorial 1'));

    // Should display details
    await waitFor(() => {
      expect(screen.getByText('Title:')).toBeInTheDocument();
      expect(screen.getByText('Description:')).toBeInTheDocument();
      expect(screen.getByText('Status:')).toBeInTheDocument();
      expect(screen.getByText('Tutorial 1')).toBeInTheDocument();
      expect(screen.getByText('Desc 1')).toBeInTheDocument();
    });
  });

  test('should show placeholder when no tutorial is selected', async () => {
    renderComponent();

    await waitFor(() => {
      expect(screen.getByText('Tutorial 1')).toBeInTheDocument();
    });

    // Should show placeholder initially
    expect(screen.getByText('Please click on a Tutorial...')).toBeInTheDocument();
  });

  test('should call deleteAll when Remove All button is clicked', async () => {
    renderComponent();

    await waitFor(() => {
      expect(screen.getByText('Tutorial 1')).toBeInTheDocument();
    });

    // Click Remove All button
    const removeAllButton = screen.getByText('Remove All');
    fireEvent.click(removeAllButton);

    await waitFor(() => {
      expect(TutorialDataService.deleteAll).toHaveBeenCalled();
    });
  });

  test('should refresh list after deleting all tutorials', async () => {
    TutorialDataService.deleteAll.mockResolvedValue({ data: {} });
    TutorialDataService.getAll
      .mockResolvedValueOnce({ data: mockTutorials })
      .mockResolvedValueOnce({ data: [] });

    renderComponent();

    await waitFor(() => {
      expect(screen.getByText('Tutorial 1')).toBeInTheDocument();
    });

    // Click Remove All
    fireEvent.click(screen.getByText('Remove All'));

    // Should call getAll again to refresh
    await waitFor(() => {
      expect(TutorialDataService.getAll).toHaveBeenCalledTimes(2);
    });
  });
});
